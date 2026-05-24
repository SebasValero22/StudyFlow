package com.example.studyflow.ui.settings;

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
import com.example.studyflow.data.dto.UserResponseDTO;

public class UserConfigFragment extends Fragment {
    private FragmentUserConfigBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserConfigBinding.inflate(inflater, container, false);

        UserResponseDTO currentUser = UserSession.getInstance().getUser();

        if (currentUser != null) {
            binding.tvUserName.setText(currentUser.getName());
            binding.tvUserEmail.setText(currentUser.getEmail());
            binding.tvUserSince.setVisibility(View.GONE);
        }

        binding.btnLogout.setOnClickListener(v -> {
            UserSession.getInstance().logout();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return binding.getRoot();
    }
}
