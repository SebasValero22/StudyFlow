package com.example.studyflow.ui.tasks;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.dto.TaskResponseDTO;
import com.example.studyflow.databinding.ItemTaskBinding;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskResponseDTO> taskList = new ArrayList<>();
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onEdit(TaskResponseDTO task);
        void onDelete(TaskResponseDTO task);
        void onToggle(TaskResponseDTO task);
    }

    public TaskAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<TaskResponseDTO> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskResponseDTO task = taskList.get(position);

        holder.binding.tvTaskTitle.setText(task.getTitle());
        
        // Color de la asignatura
        String colorHex = task.getSubjectColor();
        if (colorHex != null && !colorHex.isEmpty()) {
            try {
                if (!colorHex.startsWith("#")) colorHex = "#" + colorHex;
                holder.binding.tvTaskTitle.setTextColor(Color.parseColor(colorHex));
            } catch (Exception e) {
                // Valor por defecto si falla el parseo del color
            }
        }

        // Mostrar fecha y dias restantes
        if (task.getDue_date() != null) {
            LocalDate dueDate = task.getDue_date();
            LocalDate now = LocalDate.now();
            long days = ChronoUnit.DAYS.between(now, dueDate);
            String formattedDate = dueDate.toString();
            if (days == 0) {
                holder.binding.tvTaskDate.setText(formattedDate + " (Hoy)");
            } else if (days == 1) {
                holder.binding.tvTaskDate.setText(formattedDate + " (Falta 1 día)");
            } else if (days > 1) {
                holder.binding.tvTaskDate.setText(formattedDate + " (Faltan " + days + " días)");
            } else {
                holder.binding.tvTaskDate.setText(formattedDate + " (Vencida)");
            }
        } else {
            holder.binding.tvTaskDate.setText("No date");
        }
        
        holder.binding.cbCompleted.setChecked(task.getIsCompleted() != null && task.getIsCompleted());

        // Color segun prioridad
        String priority = task.getPriority();
        if (priority != null) {
            switch (priority.toUpperCase()) {
                case "HIGH": holder.binding.viewPriority.setBackgroundColor(Color.RED); break;
                case "MEDIUM": holder.binding.viewPriority.setBackgroundColor(Color.YELLOW); break;
                case "LOW": holder.binding.viewPriority.setBackgroundColor(Color.GREEN); break;
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onEdit(task));
        holder.binding.cbCompleted.setOnClickListener(v -> {
            task.setIsCompleted(holder.binding.cbCompleted.isChecked());
            listener.onToggle(task);
        });
    }

    @Override
    public int getItemCount() { return taskList.size(); }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        ItemTaskBinding binding;
        public TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
