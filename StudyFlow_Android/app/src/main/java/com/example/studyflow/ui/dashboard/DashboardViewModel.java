package com.example.studyflow.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studyflow.data.model.Task;
import com.example.studyflow.data.model.Exam;
import com.example.studyflow.data.repository.TaskRepository;
import java.util.List;

public class DashboardViewModel extends ViewModel {
    private final TaskRepository taskRepository = new TaskRepository();
    private final MutableLiveData<Integer> _pendingTasksCount = new MutableLiveData<>();
    private final MutableLiveData<String> _nextExamDate = new MutableLiveData<>();

    public LiveData<Integer> getPendingTasksCount() { return _pendingTasksCount; }
    public LiveData<String> getNextExamDate() { return _nextExamDate; }

    public void loadDashboardData() {
        // Obtenemos tareas para contar las pendientes
        taskRepository.fetchTasks(new TaskRepository.TasksCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                int count = 0;
                for (Task t : tasks) {
                    if (t.getCompleted() != null && !t.getCompleted()) count++;
                }
                _pendingTasksCount.postValue(count);
            }

            @Override
            public void onError(String message) {
                _pendingTasksCount.postValue(0);
            }
        });

        // Aquí podrías añadir una llamada similar para Exams para obtener el más cercano
    }
}