package com.iescamp.studyflow.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iescamp.studyflow.model.Reminder;
import com.iescamp.studyflow.model.Subject;
import com.iescamp.studyflow.utils.JsonMapper;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ReminderService {

    // GET /api/reminders
    public List<Reminder> getAllReminders() throws Exception {
        HttpRequest request = ApiClient.prepareRequest("/reminders").GET().build();
        HttpResponse<String> response = ApiClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return JsonMapper.get().readValue(response.body(), new TypeReference<List<Reminder>>() {});
        }
        throw new Exception("REMINDERS COULDNT BE LOADED");
    }

    // DELETE /api/reminders/{id}
    public void deleteReminder(Integer id) throws Exception {
        HttpRequest request = ApiClient.prepareRequest("/reminders/" + id).DELETE().build();
        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("REMINDER COULDNT BE DELETED");
        }
    }

    // SAVE REMINDER
    public void saveReminder(Reminder reminder) throws Exception {
        String json = JsonMapper.get().writeValueAsString(reminder);
        HttpRequest request = ApiClient.prepareRequest("/reminders")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("REMINDER COULDNT BE SAVED");
        }
    }
    // UPDATE REMINDER
    public void updateReminder (Integer id, Reminder reminder) throws Exception{
        String json = JsonMapper.get().writeValueAsString(reminder);
        HttpRequest request = ApiClient.prepareRequest("/reminders/" + id)
                .PUT(HttpRequest.BodyPublishers.ofString(json)).build();
        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("REMINDER COULDNT BE UPDATED");
        }
    }

}