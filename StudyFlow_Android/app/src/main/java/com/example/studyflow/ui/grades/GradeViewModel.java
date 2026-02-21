package com.example.studyflow.ui.grades;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studyflow.data.model.Grade;
import com.example.studyflow.data.repository.GradeRepository; // Crear Repo
import java.util.List;

public class GradeViewModel extends ViewModel {
    private final GradeRepository repository = new GradeRepository();
    private final MutableLiveData<List<Grade>> _grades = new MutableLiveData<>();

    public LiveData<List<Grade>> getGrades() { return _grades; }

    public void loadGrades() {
        repository.fetchGrades(new GradeRepository.Callback() {
            @Override
            public void onSuccess(List<Grade> data) { _grades.postValue(data); }
            @Override
            public void onError(String msg) { }
        });
    }
}