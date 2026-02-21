package com.example.studyflow.ui.tasks;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studyflow.data.model.Task;
import com.example.studyflow.data.repository.TaskRepository;
import java.util.List;

public class TaskViewModel extends ViewModel {
    private final TaskRepository repository = new TaskRepository();
    private final MutableLiveData<List<Task>> _tasks = new MutableLiveData<>();
    private final MutableLiveData<String> _error = new MutableLiveData<>();

    public LiveData<List<Task>> getTasks() { return _tasks; }
    public LiveData<String> getError() { return _error; }

    public void loadTasks() {
        repository.fetchTasks(new TaskRepository.TasksCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                _tasks.postValue(tasks);
            }
            @Override
            public void onError(String message) {
                _error.postValue(message);
            }
        });
    }
}