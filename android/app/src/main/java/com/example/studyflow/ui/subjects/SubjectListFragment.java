package com.example.studyflow.ui.subjects;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.studyflow.R;
import com.example.studyflow.data.dto.SubjectResponseDTO;
import com.example.studyflow.databinding.FragmentSubjectListBinding;
import com.example.studyflow.utils.UserSession;

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

        // Al hacer click en una asignatura, abrimos el dialogo de edicion
        adapter.setOnSubjectClickListener(subject -> showSubjectDialog(subject));

        // Al hacer click en el FAB, abrimos el dialogo para crear una nueva
        binding.fabAddSubject.setOnClickListener(v -> showSubjectDialog(null));

        viewModel.getSubjects().observe(getViewLifecycleOwner(), subjects -> {
            adapter.setSubjects(subjects);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        viewModel.loadSubjects();
        return binding.getRoot();
    }

    private void showSubjectDialog(SubjectResponseDTO subjectToEdit) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_subject, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        TextView tvTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText etName = dialogView.findViewById(R.id.etSubjectName);
        Button btnDelete = dialogView.findViewById(R.id.btnDeleteSubject);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelSubject);
        Button btnSave = dialogView.findViewById(R.id.btnSaveSubject);

        final View[] colorViews = {
                dialogView.findViewById(R.id.colorBlue),
                dialogView.findViewById(R.id.colorGreen),
                dialogView.findViewById(R.id.colorRed),
                dialogView.findViewById(R.id.colorYellow),
                dialogView.findViewById(R.id.colorPurple),
                dialogView.findViewById(R.id.colorOrange)
        };
        final String[] colors = {"#2196F3", "#4CAF50", "#F44336", "#FFC107", "#9C27B0", "#FF9800"};
        final String[] selectedColor = {subjectToEdit != null && subjectToEdit.getColor() != null ? subjectToEdit.getColor() : colors[0]};

        // Actualizamos los circulos de color inicialmente
        updateColorCircles(colorViews, colors, selectedColor[0]);

        // Manejamos la seleccion de color al pulsar
        for (int i = 0; i < colorViews.length; i++) {
            final int idx = i;
            colorViews[i].setOnClickListener(v -> {
                selectedColor[0] = colors[idx];
                updateColorCircles(colorViews, colors, selectedColor[0]);
            });
        }

        if (subjectToEdit == null) {
            // Modo de creacion
            tvTitle.setText("New Subject");
            btnDelete.setVisibility(View.GONE);
            
            btnSave.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) {
                    etName.setError("Name is required");
                    return;
                }
                
                Integer userId = UserSession.getInstance().isLoggedIn() ? 
                        UserSession.getInstance().getUser().getUserId() : null;

                SubjectResponseDTO newSubject = new SubjectResponseDTO();
                newSubject.setNameSubject(name);
                newSubject.setColor(selectedColor[0]);
                newSubject.setUserId(userId);
                newSubject.setActiveSubject(true);
                newSubject.setAcademicYear("2025-2026");

                viewModel.createSubject(newSubject);
                dialog.dismiss();
            });
        } else {
            // Modo de edicion
            tvTitle.setText("Edit Subject");
            etName.setText(subjectToEdit.getNameSubject());
            btnDelete.setVisibility(View.VISIBLE);

            btnDelete.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(getContext())
                        .setTitle("Delete Subject")
                        .setMessage("Are you sure you want to delete this subject? All associated data might be affected.")
                        .setPositiveButton("Delete", (dialogInterface, which) -> {
                            viewModel.deleteSubject(subjectToEdit.getSubjectId());
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            btnSave.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) {
                    etName.setError("Name is required");
                    return;
                }
                
                Integer userId = UserSession.getInstance().isLoggedIn() ? 
                        UserSession.getInstance().getUser().getUserId() : null;

                SubjectResponseDTO updatedSubject = new SubjectResponseDTO();
                updatedSubject.setSubjectId(subjectToEdit.getSubjectId());
                updatedSubject.setNameSubject(name);
                updatedSubject.setColor(selectedColor[0]);
                updatedSubject.setUserId(userId);
                updatedSubject.setActiveSubject(subjectToEdit.getActiveSubject());
                updatedSubject.setAcademicYear(subjectToEdit.getAcademicYear());

                viewModel.updateSubject(subjectToEdit.getSubjectId(), updatedSubject);
                dialog.dismiss();
            });
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateColorCircles(View[] views, String[] colors, String selectedColor) {
        for (int i = 0; i < views.length; i++) {
            View view = views[i];
            if (view == null) continue;
            String colorHex = colors[i];
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(Color.parseColor(colorHex));
            if (selectedColor.equalsIgnoreCase(colorHex)) {
                gd.setStroke(6, Color.parseColor("#2C3E50")); // Borde grueso para el seleccionado
            } else {
                gd.setStroke(2, Color.parseColor("#BDC3C7")); // Borde estandar
            }
            view.setBackground(gd);
        }
    }
}
