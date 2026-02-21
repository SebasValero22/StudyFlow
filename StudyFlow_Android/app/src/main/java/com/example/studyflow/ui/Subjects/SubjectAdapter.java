package com.example.studyflow.ui.Subjects;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.model.Subject;
import com.example.studyflow.databinding.ItemSubjectBinding; // Layout XML de cada fila
import java.util.ArrayList;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<Subject> subjectList = new ArrayList<>();

    public void setSubjects(List<Subject> subjects) {
        this.subjectList = subjects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubjectBinding binding = ItemSubjectBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SubjectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        holder.binding.tvSubjectName.setText(subject.getNameSubject());
        holder.binding.tvAcademicYear.setText(subject.getAcademicYear());

        // Parsear el color Hex (ej: "#FF5733") y ponerlo de fondo en una vista indicador
        try {
            holder.binding.viewColorIndicator.setBackgroundColor(Color.parseColor(subject.getColor()));
        } catch (IllegalArgumentException e) {
            holder.binding.viewColorIndicator.setBackgroundColor(Color.GRAY); // Color por defecto si falla
        }
    }

    @Override
    public int getItemCount() { return subjectList.size(); }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        ItemSubjectBinding binding;
        public SubjectViewHolder(ItemSubjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}