package com.iescamp.studyflow_api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iescamp.studyflow_api.dto.NyraActionResponseDTO;
import com.iescamp.studyflow_api.dto.NyraIngestRequestDTO;
import com.iescamp.studyflow_api.dto.NyraScheduleResponseDTO;
import com.iescamp.studyflow_api.model.LifeArea;
import com.iescamp.studyflow_api.model.Task;
import com.iescamp.studyflow_api.model.enums.Priority;
import com.iescamp.studyflow_api.repository.LifeAreaRepository;
import com.iescamp.studyflow_api.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NyraService {

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private LifeAreaRepository lifeAreaRepository;

    @Value("${nyra.gemini.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String geminiEndpoint;

    @Value("${nyra.gemini.key:MOCK_KEY}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public NyraService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    private static final String NYRA_SYSTEM_PROMPT = 
        "You are Nyra, the AI orchestrator for StudyFlow. " +
        "Your job is to extract actionable intents from the user's input and return ONLY a strict JSON object. " +
        "Do not include any markdown formatting, conversational text, or explanations. " +
        "Output format must match this schema: " +
        "{" +
        "  \"action\": \"CREATE|UPDATE|DELETE|AUTO_SCHEDULE|NONE\"," +
        "  \"intentTitle\": \"String\"," +
        "  \"description\": \"String\"," +
        "  \"suggestedLifeArea\": \"String\"," +
        "  \"suggestedProject\": \"String\"," +
        "  \"suggestedStartDate\": \"YYYY-MM-DD\"," +
        "  \"suggestedDueDate\": \"YYYY-MM-DD\"," +
        "  \"priority\": \"HIGH|MEDIUM|LOW\"," +
        "  \"isHabit\": boolean" +
        "}";

    public Task processIngestRequest(NyraIngestRequestDTO request, Integer userId) {
        try {
            String prompt = NYRA_SYSTEM_PROMPT + "\n\nUser Input: " + request.getText();
            NyraActionResponseDTO nyraResponse = callGeminiForIngest(prompt);

            if ("CREATE".equalsIgnoreCase(nyraResponse.getAction())) {
                return createTaskFromNyra(nyraResponse, userId);
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to process Nyra ingestion: " + e.getMessage());
        }
    }

    @Autowired(required = false)
    private CalendarService calendarService;

    public int autoScheduleInbox(Integer userId) {
        try {
            List<Task> inboxTasks = taskRepository.findInboxTasksByUser(userId);
            if (inboxTasks.isEmpty()) return 0;
            
            String taskListStr = inboxTasks.stream()
                .map(t -> "ID: " + t.getTaskId() + " | Title: " + t.getTitle() + " | Desc: " + t.getDescription())
                .collect(Collectors.joining("\n"));
                
            String today = LocalDate.now().toString();

            // Real calendar load shapes the plan: heavily-booked days get fewer tasks.
            StringBuilder calendarContext = new StringBuilder();
            try {
                if (calendarService != null) {
                    Map<LocalDate, Double> busyHours =
                            calendarService.busyHoursPerDay(userId, LocalDate.now(), 7);
                    calendarContext.append("\n\nUser's existing calendar commitments (busy hours per day):\n");
                    busyHours.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .forEach(e -> calendarContext.append(e.getKey())
                                    .append(": ").append(String.format("%.1f", e.getValue())).append("h booked\n"));
                    calendarContext.append("Avoid overloading days with more than 6 booked hours.\n");
                }
            } catch (Exception e) {
                // Calendar context is an enhancement, never a blocker.
                e.printStackTrace();
            }

            String prompt = "You are Nyra, the AI auto-scheduler for StudyFlow. Today is " + today + ". " +
                "Read the following unassigned tasks and schedule them over the next 7 days based on priority and logical grouping. " +
                calendarContext +
                "You MUST return ONLY a strict JSON array of objects matching this schema exactly, with NO markdown formatting:\n" +
                "[\n" +
                "  {\n" +
                "    \"taskId\": Integer,\n" +
                "    \"suggestedDueDate\": \"YYYY-MM-DD\",\n" +
                "    \"orderIndex\": Integer,\n" +
                "    \"suggestedLifeArea\": \"String\"\n" +
                "  }\n" +
                "]\n\nTasks to schedule:\n" + taskListStr;

            List<NyraScheduleResponseDTO> schedulePlan = callGeminiForAutoSchedule(prompt);
            
            if (schedulePlan == null || schedulePlan.isEmpty()) return 0;

            int updatedCount = 0;
            for (NyraScheduleResponseDTO plan : schedulePlan) {
                Task t = taskRepository.findById(plan.getTaskId()).orElse(null);
                if (t != null) {
                    if (plan.getSuggestedDueDate() != null) {
                        t.setDue_date(plan.getSuggestedDueDate());
                    }
                    if (plan.getOrderIndex() != null) {
                        t.setOrderIndex(plan.getOrderIndex());
                    }
                    if (plan.getSuggestedLifeArea() != null && !plan.getSuggestedLifeArea().isBlank()) {
                        LifeArea area = lifeAreaRepository.findByUserId(userId).stream()
                            .filter(a -> a.getName().equalsIgnoreCase(plan.getSuggestedLifeArea()))
                            .findFirst().orElse(null);
                        if (area != null) {
                            t.setLifeArea(area);
                        }
                    }
                    updatedCount++;
                }
            }
            
            taskRepository.saveAll(inboxTasks);
            return updatedCount;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to auto-schedule: " + e.getMessage());
        }
    }

    private NyraActionResponseDTO callGeminiForIngest(String prompt) {
        String jsonResult = executeGeminiRequest(prompt);
        try {
            return objectMapper.readValue(jsonResult, NyraActionResponseDTO.class);
        } catch (Exception e) {
            return mockFallbackParse(prompt);
        }
    }
    
    private List<NyraScheduleResponseDTO> callGeminiForAutoSchedule(String prompt) {
        String jsonResult = executeGeminiRequest(prompt);
        try {
            return objectMapper.readValue(jsonResult, new TypeReference<List<NyraScheduleResponseDTO>>(){});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String executeGeminiRequest(String prompt) {
        if ("MOCK_KEY".equals(geminiApiKey)) {
            // Simulated mock for testing without real key
            return mockGeminiResponse(prompt);
        }
        
        try {
            String url = geminiEndpoint + "?key=" + geminiApiKey;
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                    "responseMimeType", "application/json"
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        String text = (String) parts.get(0).get("text");
                        // Strip markdown formatting if Gemini didn't honor the system prompt
                        if (text.startsWith("```json")) {
                            text = text.substring(7, text.length() - 3).trim();
                        }
                        return text;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }

    private String mockGeminiResponse(String prompt) {
        // Return dummy JSON array to satisfy the parser for testing
        if (prompt.contains("auto-scheduler")) {
            return "[ { \"taskId\": 1, \"suggestedDueDate\": \"2026-06-20\", \"orderIndex\": 0, \"suggestedLifeArea\": \"Academics\" } ]";
        }
        return "{ \"action\": \"CREATE\", \"intentTitle\": \"Mock Task\", \"priority\": \"MEDIUM\", \"isHabit\": false }";
    }

    private NyraActionResponseDTO mockFallbackParse(String prompt) {
        NyraActionResponseDTO mock = new NyraActionResponseDTO();
        mock.setAction("CREATE");
        mock.setIntentTitle("Mock Task");
        mock.setPriority("MEDIUM");
        mock.setIsHabit(false);
        return mock;
    }

    private Task createTaskFromNyra(NyraActionResponseDTO dto, Integer userId) {
        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(dto.getIntentTitle());
        task.setDescription(dto.getDescription());
        task.setStart_date(dto.getSuggestedStartDate());
        task.setDue_date(dto.getSuggestedDueDate());
        
        try {
            if (dto.getPriority() != null) {
                task.setPriority(Priority.valueOf(dto.getPriority().toUpperCase()));
            } else {
                task.setPriority(Priority.MEDIUM);
            }
        } catch (IllegalArgumentException e) {
            task.setPriority(Priority.MEDIUM);
        }

        task.setIsHabit(dto.getIsHabit() != null ? dto.getIsHabit() : false);
        task.setCompleted(false);
        task.setOrderIndex(0);

        return taskRepository.save(task);
    }
}
