package com.example.studyflow.ui.tasks;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.dto.TaskResponseDTO;
import com.example.studyflow.databinding.ItemTaskBinding;
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
        holder.binding.tvTaskDate.setText(task.getDue_date() != null ? task.getDue_date().toString() : "No date");
        holder.binding.cbCompleted.setChecked(task.getIsCompleted() != null && task.getIsCompleted());

        // Priority Color
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
