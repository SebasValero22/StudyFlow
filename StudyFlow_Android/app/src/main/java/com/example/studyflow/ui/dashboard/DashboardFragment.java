package com.example.studyflow.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.studyflow.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Observar cambios en el conteo de tareas
        viewModel.getPendingTasksCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvTaskCount.setText(String.valueOf(count));
        });

        // Cargar datos al iniciar
        viewModel.loadDashboardData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}