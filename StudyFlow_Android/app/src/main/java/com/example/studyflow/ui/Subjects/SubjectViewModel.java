package com.example.studyflow.ui.Subjects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.studyflow.data.model.Subject;
import com.example.studyflow.data.repository.SubjectRepository;

import java.util.List;

public class SubjectViewModel extends ViewModel {

    // Instancia del repositorio que maneja la conexión con la API
    private final SubjectRepository repository = new SubjectRepository();

    // LiveData para observar la lista de asignaturas desde el Fragment
    private final MutableLiveData<List<Subject>> _subjects = new MutableLiveData<>();

    // LiveData para manejar errores (opcional pero recomendado)
    private final MutableLiveData<String> _error = new MutableLiveData<>();

    // Getters públicos para que el Fragment pueda observar los datos (solo lectura)
    public LiveData<List<Subject>> getSubjects() {
        return _subjects;
    }

    public LiveData<String> getError() {
        return _error;
    }

    // Método para cargar los datos desde la API
    public void loadSubjects() {
        repository.fetchSubjects(new SubjectRepository.Callback() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                // PostValue se usa porque la respuesta viene en un hilo de fondo
                _subjects.postValue(subjects);
            }

            @Override
            public void onError(String message) {
                _error.postValue(message);
            }
        });
    }
}