package com.example.studyflow.ui.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.studyflow.data.dto.TaskResponseDTO;
import com.example.studyflow.data.dto.SubjectResponseDTO;
import com.example.studyflow.data.repository.SubjectRepository;
import com.example.studyflow.data.repository.TaskRepository;
import com.example.studyflow.databinding.ActivityTaskEditBinding;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskEditActivity extends AppCompatActivity {
    private ActivityTaskEditBinding binding;
    private TaskRepository repository;
    private SubjectRepository subjectRepository;
    private List<SubjectResponseDTO> subjectsList = new ArrayList<>();
    private LocalDate selectedDate = LocalDate.now();
    private Integer editingTaskId = null;
    private Integer editingSubjectId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar Toolbar como ActionBar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        repository = new TaskRepository();
        subjectRepository = new SubjectRepository();

        // Configurar spinner de prioridad
        String[] priorities = {"LOW", "MEDIUM", "HIGH"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPriority.setAdapter(adapter);

        // Cargar asignaturas para el spinner
        loadSubjects();

        // Comprobar si estamos editando
        if (getIntent().hasExtra("taskId")) {
            editingTaskId = getIntent().getIntExtra("taskId", -1);
            editingSubjectId = getIntent().getIntExtra("subjectId", -1);
            binding.tvHeader.setText("Edit Task");
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Task");
            binding.etTitle.setText(getIntent().getStringExtra("title"));
            binding.etDescription.setText(getIntent().getStringExtra("description"));
            String priority = getIntent().getStringExtra("priority");
            if (priority != null) {
                for (int i = 0; i < priorities.length; i++) {
                    if (priorities[i].equalsIgnoreCase(priority)) {
                        binding.spinnerPriority.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("New Task");
        }

        binding.btnDatePicker.setOnClickListener(v -> showDatePicker());
        binding.btnSave.setOnClickListener(v -> saveTask());
    }

    private void loadSubjects() {
        subjectRepository.getSubjects(new TaskRepository.RepositoryCallback<List<SubjectResponseDTO>>() {
            @Override
            public void onSuccess(List<SubjectResponseDTO> result) {
                subjectsList = result;
                List<String> names = new ArrayList<>();
                int selection = 0;
                for (int i = 0; i < result.size(); i++) {
                    names.add(result.get(i).getNameSubject());
                    if (editingSubjectId != null && result.get(i).getSubjectId().equals(editingSubjectId)) {
                        selection = i;
                    }
                }
                ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(TaskEditActivity.this, 
                        android.R.layout.simple_spinner_item, names);
                subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerSubject.setAdapter(subjectAdapter);
                binding.spinnerSubject.setSelection(selection);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(TaskEditActivity.this, "Error loading subjects: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            selectedDate = LocalDate.of(year1, monthOfYear + 1, dayOfMonth);
            binding.btnDatePicker.setText(selectedDate.toString());
        }, year, month, day);
        datePickerDialog.show();
    }

    private void saveTask() {
        if (subjectsList.isEmpty() || binding.spinnerSubject.getSelectedItemPosition() == -1) {
            Toast.makeText(this, "Please select a subject first", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedSubjectId = subjectsList.get(binding.spinnerSubject.getSelectedItemPosition()).getSubjectId();

        TaskResponseDTO task = new TaskResponseDTO();
        task.setTitle(binding.etTitle.getText().toString());
        task.setDescription(binding.etDescription.getText().toString());
        task.setDue_date(selectedDate);
        task.setPriority(binding.spinnerPriority.getSelectedItem().toString());
        task.setIsCompleted(false);
        task.setSubjectId(selectedSubjectId);

        if (editingTaskId == null) {
            repository.createTask(task, new TaskRepository.RepositoryCallback<TaskResponseDTO>() {
                @Override
                public void onSuccess(TaskResponseDTO result) {
                    finish();
                }
                @Override
                public void onError(String message) {
                    Toast.makeText(TaskEditActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            repository.updateTask(editingTaskId, task, new TaskRepository.RepositoryCallback<TaskResponseDTO>() {
                @Override
                public void onSuccess(TaskResponseDTO result) {
                    finish();
                }
                @Override
                public void onError(String message) {
                    Toast.makeText(TaskEditActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
