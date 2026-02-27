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
            // 1. Cargar conteo de tareas pendientes
            List<Task> tasks = taskService.getAllTasks();
            long pendingTasks = tasks.stream().filter(t -> !t.getIsCompleted()).count();
            lblTasksCount.setText(pendingTasks + " Tareas pendientes");

            // 2. Cargar el próximo examen
            List<Exam> exams = examService.getAllExams();
            if (!exams.isEmpty()) {
                // Asumimos que la API los devuelve ordenados o tomamos el primero
                lblNextExam.setText("Próximo: " + exams.get(0).getNameExam());
            } else {
                lblNextExam.setText("No hay exámenes");
            }

            // 3. Calcular GPA (Media)
            List<Grade> grades = gradeService.getAllGrades();
            if (!grades.isEmpty()) {
                double avg = grades.stream()
                        .mapToDouble(g -> g.getScore() * (g.getWeight() / 100.0))
                        .sum();
                lblGPA.setText(String.format("Media Total: %.2f", avg));
            } else {
                lblGPA.setText("Sin notas");
            }

        } catch (Exception e) {
            System.err.println("Error cargando estadísticas: " + e.getMessage());
            lblTasksCount.setText("Error al cargar");
            lblNextExam.setText("Error al cargar");
            lblGPA.setText("Error al cargar");
        }
    }
}