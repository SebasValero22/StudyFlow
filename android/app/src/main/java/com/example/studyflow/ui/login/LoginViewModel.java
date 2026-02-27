package com.example.studyflow.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studyflow.data.dto.UserResponseDTO;
import com.example.studyflow.data.repository.UserRepository;
import com.example.studyflow.utils.UserSession;

public class LoginViewModel extends ViewModel {
    private final UserRepository repository = new UserRepository();
    private final MutableLiveData<UserResponseDTO> _user = new MutableLiveData<>();
    private final MutableLiveData<String> _error = new MutableLiveData<>();

    public LiveData<UserResponseDTO> getUser() { return _user; }
    public LiveData<String> getError() { return _error; }

    public void login(String email, String password) {
        repository.loginUser(email, password, new UserRepository.LoginCallback() {
            @Override
            public void onSuccess(UserResponseDTO user) {
                UserSession.getInstance().setUser(user);
                _user.postValue(user);
            }

            @Override
            public void onError(String message) {
                _error.postValue(message);
            }
        });
    }

    public void register(String name, String email, String password) {
        repository.registerUser(name, email, password, new UserRepository.LoginCallback() {
            @Override
            public void onSuccess(UserResponseDTO user) {
                UserSession.getInstance().setUser(user);
                _user.postValue(user);
            }

            @Override
            public void onError(String message) {
                _error.postValue(message);
            }
        });
    }
}
