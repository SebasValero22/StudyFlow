package com.example.studyflow.ui.Subjects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.studyflow.databinding.FragmentSubjectListBinding;
import com.example.studyflow.ui.Subjects.SubjectAdapter;
import com.example.studyflow.ui.Subjects.SubjectViewModel;


public class SubjectListFragment extends Fragment {

    private FragmentSubjectListBinding binding;
    private SubjectViewModel viewModel;
    private SubjectAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSubjectListBinding.inflate(inflater, container, false);

        // Inicializar el ViewModel
        viewModel = new ViewModelProvider(this).get(SubjectViewModel.class);

        // Configurar RecyclerView y Adapter
        adapter = new SubjectAdapter();
        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSubjects.setAdapter(adapter);

        // Observar cambios en la lista de asignaturas
        viewModel.getSubjects().observe(getViewLifecycleOwner(), subjects -> {
            adapter.setSubjects(subjects);
        });

        // Cargar datos
        viewModel.loadSubjects();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}