package com.example.studyflow.ui.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.studyflow.data.dto.TaskResponseDTO;
import com.example.studyflow.databinding.FragmentTaskListBinding;

public class TaskListFragment extends Fragment implements TaskAdapter.OnTaskClickListener {
    private FragmentTaskListBinding binding;
    private TaskViewModel viewModel;
    private TaskAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTaskListBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(this);
        binding.recyclerViewTasks.setAdapter(adapter);

        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> adapter.submitList(tasks));
        viewModel.getError().observe(getViewLifecycleOwner(), error -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());

        binding.fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), TaskEditActivity.class);
            startActivity(intent);
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadTasks();
    }

    @Override
    public void onEdit(TaskResponseDTO task) {
        Intent intent = new Intent(getContext(), TaskEditActivity.class);
        intent.putExtra("taskId", task.getTaskId());
        intent.putExtra("title", task.getTitle());
        intent.putExtra("description", task.getDescriptionTask());
        startActivity(intent);
    }

    @Override
    public void onDelete(TaskResponseDTO task) {
        viewModel.deleteTask(task.getTaskId());
    }

    @Override
    public void onToggle(TaskResponseDTO task) {
        // Implement toggle logic in ViewModel
    }
}
