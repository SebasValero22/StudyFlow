package com.example.studyflow.ui.grades;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.model.Grade;
import com.example.studyflow.databinding.ItemGradeBinding; // Generado por item_grade.xml
import java.util.ArrayList;
import java.util.List;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {

    private List<Grade> gradeList = new ArrayList<>();

    public void setGrades(List<Grade> grades) {
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
        Grade grade = gradeList.get(position);

        holder.binding.tvConcept.setText(grade.getConcept());
        holder.binding.tvScore.setText(String.valueOf(grade.getScore()));

        // Formato: "Peso: 30% | 2023-10-15"
        String details = "Peso: " + (grade.getWeight() * 100) + "%";
        if (grade.getGradeDate() != null) {
            details += " | " + grade.getGradeDate().toString();
        }
        holder.binding.tvDetails.setText(details);

        // Lógica visual: Verde si aprueba (>=5), Rojo si suspende (<5)
        if (grade.getScore() >= 5.0) {
            holder.binding.tvScore.setTextColor(Color.parseColor("#4CAF50")); // Verde
        } else {
            holder.binding.tvScore.setTextColor(Color.parseColor("#F44336")); // Rojo
        }
    }

    @Override
    public int getItemCount() {
        return gradeList != null ? gradeList.size() : 0;
    }

    static class GradeViewHolder extends RecyclerView.ViewHolder {
        ItemGradeBinding binding;

        public GradeViewHolder(ItemGradeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}