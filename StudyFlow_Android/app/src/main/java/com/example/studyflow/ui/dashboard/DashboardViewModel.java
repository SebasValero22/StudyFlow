package com.example.studyflow.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studyflow.data.dto.TaskResponseDTO;
import com.example.studyflow.data.repository.TaskRepository;
import java.util.List;

public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<Integer> mTaskCount = new MutableLiveData<>();
    private final TaskRepository taskRepository = new TaskRepository();

    public DashboardViewModel() {
        loadSummary();
    }

    public LiveData<Integer> getTaskCount() {
        return mTaskCount;
    }

    public void loadSummary() {
        taskRepository.getTasks(new TaskRepository.RepositoryCallback<List<TaskResponseDTO>>() {
            @Override
            public void onSuccess(List<TaskResponseDTO> result) {
                if (result != null) {
                    mTaskCount.postValue(result.size());
                } else {
                    mTaskCount.postValue(0);
                }
            }

            @Override
            public void onError(String message) {
                mTaskCount.postValue(0);
            }
        });
    }
}
