package com.example.studyflow.ui.subjects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studyflow.data.dto.SubjectResponseDTO;
import com.example.studyflow.data.repository.SubjectRepository;
import com.example.studyflow.data.repository.TaskRepository;
import java.util.List;

public class SubjectViewModel extends ViewModel {
    private final SubjectRepository repository = new SubjectRepository();
    private final MutableLiveData<List<SubjectResponseDTO>> subjects = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<SubjectResponseDTO>> getSubjects() { return subjects; }
    public LiveData<String> getError() { return error; }

    public void loadSubjects() {
        repository.getSubjects(new TaskRepository.RepositoryCallback<List<SubjectResponseDTO>>() {
            @Override
            public void onSuccess(List<SubjectResponseDTO> result) {
                subjects.postValue(result);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void createSubject(SubjectResponseDTO subject) {
        repository.createSubject(subject, new TaskRepository.RepositoryCallback<SubjectResponseDTO>() {
            @Override
            public void onSuccess(SubjectResponseDTO result) {
                loadSubjects();
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void updateSubject(Integer id, SubjectResponseDTO subject) {
        repository.updateSubject(id, subject, new TaskRepository.RepositoryCallback<SubjectResponseDTO>() {
            @Override
            public void onSuccess(SubjectResponseDTO result) {
                loadSubjects();
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void deleteSubject(Integer id) {
        repository.deleteSubject(id, new TaskRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadSubjects();
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }
}
