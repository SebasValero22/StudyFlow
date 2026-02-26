package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Grade;
import com.iescamp.studyflow.model.Task;
import com.iescamp.studyflow.model.Exam;
import com.iescamp.studyflow.service.GradeService;
import com.iescamp.studyflow.service.TaskService;
import com.iescamp.studyflow.service.ExamService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.List;

public class OverviewController {

    @FXML private Label lblTasksCount;
    @FXML private Label lblNextExam;
    @FXML private Label lblGPA;

    private final TaskService taskService = new TaskService();
    private final ExamService examService = new ExamService();
    private final GradeService gradeService = new GradeService();

    @FXML
    public void initialize() {
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            // 1. Pending tasks count
            List<Task> tasks = taskService.getAllTasks();
            long pendingTasks = tasks.stream()
                    .filter(t -> t.getIsCompleted() == null || !t.getIsCompleted())
                    .count();
            lblTasksCount.setText(String.valueOf(pendingTasks));

            // 2. Next exam
            List<Exam> exams = examService.getAllExams();
            if (!exams.isEmpty()) {
                lblNextExam.setText(exams.get(0).getNameExam());
            } else {
                lblNextExam.setText("No exams scheduled");
            }

            // 3. GPA Calculation
            List<Grade> grades = gradeService.getAllGrades();
            if (!grades.isEmpty()) {
                double totalPoints = 0;
                double totalWeight = 0;
                for (Grade g : grades) {
                    totalPoints += g.getScore() * (g.getWeight() / 100.0);
                    totalWeight += (g.getWeight() / 100.0);
                }
                double finalGPA = totalWeight > 0 ? (totalPoints / totalWeight) : 0;
                lblGPA.setText(String.format("%.2f", finalGPA));
            } else {
                lblGPA.setText("0.00");
            }

        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            lblTasksCount.setText("Error loading");
            lblNextExam.setText("Error loading");
            lblGPA.setText("Error loading");
        }
    }
}