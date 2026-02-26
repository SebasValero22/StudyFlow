package com.example.studyflow.ui.grades;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studyflow.data.dto.GradeResponseDTO;
import com.example.studyflow.data.repository.GradeRepository;
import com.example.studyflow.data.repository.TaskRepository;
import java.util.List;

public class GradeViewModel extends ViewModel {
    private final GradeRepository repository = new GradeRepository();
    private final MutableLiveData<List<GradeResponseDTO>> grades = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<GradeResponseDTO>> getGrades() { return grades; }
    public LiveData<String> getError() { return error; }

    public void loadGrades() {
        repository.getGrades(new TaskRepository.RepositoryCallback<List<GradeResponseDTO>>() {
            @Override
            public void onSuccess(List<GradeResponseDTO> result) {
                grades.postValue(result);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }
}
