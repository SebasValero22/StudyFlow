package com.example.studyflow.ui.subjects;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.dto.SubjectResponseDTO;
import com.example.studyflow.databinding.ItemSubjectBinding;
import java.util.ArrayList;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<SubjectResponseDTO> subjectList = new ArrayList<>();

    public void setSubjects(List<SubjectResponseDTO> subjects) {
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
        SubjectResponseDTO subject = subjectList.get(position);
        holder.binding.tvSubjectName.setText(subject.getNameSubject());
        
        if (subject.getColor() != null && !subject.getColor().isEmpty()) {
            try {
                String colorHex = subject.getColor();
                if (!colorHex.startsWith("#")) colorHex = "#" + colorHex;
                int color = Color.parseColor(colorHex);
                holder.binding.tvSubjectName.setTextColor(color);
                // Hide the indicator as requested (color out of column)
                holder.binding.viewColorIndicator.setVisibility(View.GONE);
            } catch (Exception e) {
                // Fallback
            }
        }
        
        holder.binding.tvAcademicYear.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        ItemSubjectBinding binding;
        public SubjectViewHolder(ItemSubjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
