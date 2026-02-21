package com.example.studyflow.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.studyflow.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel viewModel;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString();
            String pass = binding.etPassword.getText().toString();
            viewModel.login(email, pass);
        });

        viewModel.getUser().observe(this, user -> {
            // Ir al Dashboard al tener éxito
            // startActivity(new Intent(this, DashboardActivity.class));
            Toast.makeText(this, "Bienvenido " + user.getUserName(), Toast.LENGTH_SHORT).show();
        });

        viewModel.getError().observe(this, error ->
                Toast.makeText(this, error, Toast.LENGTH_LONG).show());
    }
}