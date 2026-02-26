package com.example.studyflow.ui.grades;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.dto.GradeResponseDTO;
import com.example.studyflow.databinding.ItemGradeBinding;
import java.util.ArrayList;
import java.util.List;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {

    private List<GradeResponseDTO> gradeList = new ArrayList<>();

    public void setGrades(List<GradeResponseDTO> grades) {
        this.gradeList = grades;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGradeBinding binding = ItemGradeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GradeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
        GradeResponseDTO grade = gradeList.get(position);
        holder.binding.tvConcept.setText(grade.getConcept());
        holder.binding.tvScore.setText(String.valueOf(grade.getScore()));
        
        String dateStr = grade.getGradeDate() != null ? grade.getGradeDate().toString() : "";
        String weightStr = grade.getWeight() != null ? (grade.getWeight() * 100) + "%" : "0%";
        holder.binding.tvDetails.setText("Peso: " + weightStr + " | " + dateStr);
    }

    @Override
    public int getItemCount() {
        return gradeList.size();
    }

    static class GradeViewHolder extends RecyclerView.ViewHolder {
        ItemGradeBinding binding;
        public GradeViewHolder(ItemGradeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
