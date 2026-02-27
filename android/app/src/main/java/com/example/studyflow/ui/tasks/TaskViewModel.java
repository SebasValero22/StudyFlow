package com.example.studyflow.ui.tasks;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studyflow.data.dto.TaskResponseDTO;
import com.example.studyflow.data.repository.TaskRepository;
import java.util.List;

public class TaskViewModel extends ViewModel {
    private final TaskRepository repository = new TaskRepository();
    private final MutableLiveData<List<TaskResponseDTO>> _tasks = new MutableLiveData<>();
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>();

    public LiveData<List<TaskResponseDTO>> getTasks() { return _tasks; }
    public LiveData<String> getError() { return _error; }
    public LiveData<Boolean> getLoading() { return _loading; }

    public void loadTasks() {
        _loading.setValue(true);
        repository.getTasks(new TaskRepository.RepositoryCallback<List<TaskResponseDTO>>() {
            @Override
            public void onSuccess(List<TaskResponseDTO> result) {
                _tasks.postValue(result);
                _loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                _error.postValue(message);
                _loading.postValue(false);
            }
        });
    }

    public void deleteTask(Integer id) {
        repository.deleteTask(id, new TaskRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadTasks();
            }

            @Override
            public void onError(String message) {
                _error.postValue(message);
            }
        });
    }
}
