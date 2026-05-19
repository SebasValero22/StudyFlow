package com.example.studyflow.ui.subjects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.studyflow.databinding.FragmentSubjectListBinding;

public class SubjectListFragment extends Fragment {
    private FragmentSubjectListBinding binding;
    private SubjectViewModel viewModel;
    private SubjectAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSubjectListBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(SubjectViewModel.class);

        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SubjectAdapter();
        binding.rvSubjects.setAdapter(adapter);

        viewModel.getSubjects().observe(getViewLifecycleOwner(), subjects -> {
            adapter.setSubjects(subjects);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        viewModel.loadSubjects();
        return binding.getRoot();
    }
}
