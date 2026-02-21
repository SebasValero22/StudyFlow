package com.example.studyflow.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.studyflow.databinding.FragmentTaskListBinding;


public class TaskListFragment extends Fragment {
    private FragmentTaskListBinding binding;
    private TaskViewModel viewModel;
    private TaskAdapter adapter; // Debes crear este Adapter

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTaskListBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Configurar RecyclerView
        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter();
        binding.recyclerViewTasks.setAdapter(adapter);

        // Observar datos
        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            adapter.submitList(tasks); // Pasa la lista al adapter
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error ->
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show()
        );

        viewModel.loadTasks(); // Cargar datos al iniciar
        return binding.getRoot();
    }
}