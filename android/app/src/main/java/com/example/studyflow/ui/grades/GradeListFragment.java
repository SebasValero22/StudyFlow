package com.example.studyflow.ui.grades;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.studyflow.databinding.FragmentGradeListBinding;

public class GradeListFragment extends Fragment {
    private FragmentGradeListBinding binding;
    private GradeViewModel viewModel;
    private GradeAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGradeListBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(GradeViewModel.class);

        binding.rvGrades.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GradeAdapter();
        binding.rvGrades.setAdapter(adapter);

        viewModel.getGrades().observe(getViewLifecycleOwner(), grades -> {
            adapter.setGrades(grades);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        viewModel.loadGrades();
        return binding.getRoot();
    }
}
