package com.example.studyflow.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.studyflow.databinding.FragmentDashboardBinding;

import com.example.studyflow.ui.tasks.TaskListFragment;
import com.example.studyflow.R;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        viewModel.getTaskCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvTaskCount.setText(String.valueOf(count));
        });

        binding.btnViewTasks.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TaskListFragment())
                    .addToBackStack(null)
                    .commit();
        });

        viewModel.loadSummary();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
