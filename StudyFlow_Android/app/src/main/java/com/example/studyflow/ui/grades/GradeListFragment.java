package com.example.studyflow.ui.grades;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.studyflow.databinding.FragmentGradeListBinding;

public class GradeListFragment extends Fragment {
    private FragmentGradeListBinding binding;
    private GradeViewModel viewModel;
    // Asumimos un GradeAdapter similar al de Subjects
    private GradeAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGradeListBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(GradeViewModel.class);

        adapter = new GradeAdapter();
        binding.rvGrades.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvGrades.setAdapter(adapter);

        viewModel.getGrades().observe(getViewLifecycleOwner(), grades -> {
            adapter.setGrades(grades);
        });

        viewModel.loadGrades();
        return binding.getRoot();
    }
}