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

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class OverviewController {

    @FXML private Label lblTasksCount;
    @FXML private Label lblNextExam;
    @FXML private Label lblGPA;

    @FXML private ComboBox<String> timeframeCombo;
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> colTaskTitle;
    @FXML private TableColumn<Task, LocalDate> colTaskDate;

    @FXML private TableView<Exam> examsTable;
    @FXML private TableColumn<Exam, String> colExamName;
    @FXML private TableColumn<Exam, LocalDate> colExamDate;

    private final TaskService taskService = new TaskService();
    private final ExamService examService = new ExamService();
    private final GradeService gradeService = new GradeService();

    private List<Task> allTasks;
    private List<Exam> allExams;

    @FXML
    public void initialize() {
        setupTimeframeCombo();
        setupTables();
        loadData();
    }

    private void setupTimeframeCombo() {
        timeframeCombo.setItems(FXCollections.observableArrayList(
                "1 Semana", "2 Semanas", "3 Semanas", "1 Mes"
        ));
        timeframeCombo.setValue("1 Semana");
        timeframeCombo.setOnAction(event -> filterData());
    }

    private void setupTables() {
        colTaskTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTaskDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colTaskTitle.setCellFactory(column -> createColoredTaskCell());

        colExamName.setCellValueFactory(new PropertyValueFactory<>("nameExam"));
        colExamDate.setCellValueFactory(new PropertyValueFactory<>("examDate"));
        colExamName.setCellFactory(column -> createColoredExamCell());
    }

    private TableCell<Task, String> createColoredTaskCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Task task = getTableView().getItems().get(getIndex());
                    String color = task.getSubjectColor();
                    if (color != null) {
                        if (!color.startsWith("#")) color = "#" + color;
                        setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                    }
                }
            }
        };
    }

    private TableCell<Exam, String> createColoredExamCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Exam exam = getTableView().getItems().get(getIndex());
                    String color = exam.getSubjectColor();
                    if (color != null) {
                        if (!color.startsWith("#")) color = "#" + color;
                        setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                    }
                }
            }
        };
    }

    private void loadData() {
        try {
            allTasks = taskService.getAllTasks();
            allExams = examService.getAllExams();
            loadStatistics();
            filterData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterData() {
        if (allTasks == null || allExams == null) return;

        int days = switch (timeframeCombo.getValue()) {
            case "1 Semana" -> 7;
            case "2 Semanas" -> 14;
            case "3 Semanas" -> 21;
            case "1 Mes" -> 30;
            default -> 7;
        };

        LocalDate limitDate = LocalDate.now().plusDays(days);
        LocalDate today = LocalDate.now();

        List<Task> filteredTasks = allTasks.stream()
                .filter(t -> !t.getIsCompleted())
                .filter(t -> t.getDueDate() != null && !t.getDueDate().isBefore(today) && !t.getDueDate().isAfter(limitDate))
                .collect(Collectors.toList());

        List<Exam> filteredExams = allExams.stream()
                .filter(e -> e.getExamDate() != null && !e.getExamDate().isBefore(today) && !e.getExamDate().isAfter(limitDate))
                .collect(Collectors.toList());

        tasksTable.setItems(FXCollections.observableArrayList(filteredTasks));
        examsTable.setItems(FXCollections.observableArrayList(filteredExams));
    }

    private void loadStatistics() {
        try {
            long pendingTasks = allTasks.stream().filter(t -> !t.getIsCompleted()).count();
            lblTasksCount.setText(pendingTasks + " Tareas pendientes");

            if (!allExams.isEmpty()) {
                lblNextExam.setText("Próximo: " + allExams.get(0).getNameExam());
            } else {
                lblNextExam.setText("No hay exámenes");
            }

            List<Grade> grades = gradeService.getAllGrades();
            if (!grades.isEmpty()) {
                double avg = grades.stream().mapToDouble(Grade::getScore).average().orElse(0.0);
                lblGPA.setText(String.format("Media Total: %.2f", avg));
            } else {
                lblGPA.setText("Sin notas");
            }
        } catch (Exception e) {
            lblTasksCount.setText("Error");
        }
    }
}