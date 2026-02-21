package com.example.studyflow.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.studyflow.databinding.FragmentUserConfigBinding;
import com.example.studyflow.ui.login.LoginActivity;
import com.example.studyflow.utils.UserSession;
import com.example.studyflow.data.model.User;

public class UserConfigFragment extends Fragment {
    private FragmentUserConfigBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserConfigBinding.inflate(inflater, container, false);

        // Obtener datos de la sesión actual
        User currentUser = UserSession.getInstance().getUser();

        if (currentUser != null) {
            binding.tvUserName.setText(currentUser.getUserName());
            binding.tvUserEmail.setText(currentUser.getEmail());
            // Formatear fecha si es necesario
            binding.tvUserSince.setText("Miembro desde: " + currentUser.getRegistrationDate().toString());
        }

        binding.btnLogout.setOnClickListener(v -> {
            // Cerrar sesión
            UserSession.getInstance().logout();

            // Volver al Login y limpiar pila de actividades
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return binding.getRoot();
    }
}