package com.example.studyflow.ui.tasks;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.model.Task;
import com.example.studyflow.databinding.ItemTaskBinding; // Generado de item_task.xml
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList = new ArrayList<>();

    // Método para actualizar la lista desde el Fragment
    public void submitList(List<Task> tasks) {
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
        Task task = taskList.get(position);

        holder.binding.tvTaskTitle.setText(task.getTitle()); // Asegúrate de tener getTitle() en Task.java

        // Manejo de fecha (asumiendo que es LocalDate o String)
        if (task.getDueDate() != null) {
            holder.binding.tvTaskDate.setText(task.getDueDate().toString());
        } else {
            holder.binding.tvTaskDate.setText("Sin fecha");
        }

        // Checkbox
        holder.binding.cbCompleted.setChecked(task.getCompleted() != null && task.getCompleted());

        // Lógica de colores por prioridad
        String priority = task.getPriority(); // Asumiendo que devuelve "HIGH", "MEDIUM", etc.
        if (priority != null) {
            switch (priority.toUpperCase()) {
                case "HIGH":
                    holder.binding.viewPriority.setBackgroundColor(Color.parseColor("#F44336")); // Rojo
                    break;
                case "MEDIUM":
                    holder.binding.viewPriority.setBackgroundColor(Color.parseColor("#FF9800")); // Naranja
                    break;
                case "LOW":
                    holder.binding.viewPriority.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde
                    break;
                default:
                    holder.binding.viewPriority.setBackgroundColor(Color.GRAY);
            }
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        ItemTaskBinding binding;

        public TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}