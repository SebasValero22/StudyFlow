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

import com.iescamp.studyflow.model.DashboardItem;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OverviewController {

    @FXML private Label lblTasksCount;
    @FXML private Label lblNextExam;
    @FXML private Label lblGPA;

    @FXML private ComboBox<String> timeframeCombo;
    @FXML private TableView<DashboardItem> activityTable;
    @FXML private TableColumn<DashboardItem, String> colType;
    @FXML private TableColumn<DashboardItem, String> colTitle;
    @FXML private TableColumn<DashboardItem, String> colSubject;
    @FXML private TableColumn<DashboardItem, LocalDate> colDate;

    private final TaskService taskService = new TaskService();
    private final ExamService examService = new ExamService();
    private final GradeService gradeService = new GradeService();

    private List<Task> allTasks;
    private List<Exam> allExams;

    @FXML
    public void initialize() {
        setupTimeframeCombo();
        setupTable();
        loadData();
    }

    private void setupTimeframeCombo() {
        timeframeCombo.setItems(FXCollections.observableArrayList(
                "1 Week", "2 Weeks", "3 Weeks", "1 Month"
        ));
        timeframeCombo.setValue("1 Week");
        timeframeCombo.setOnAction(event -> filterData());
    }

    private void setupTable() {
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Coloring Logic for Title
        colTitle.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    DashboardItem rowItem = getTableView().getItems().get(getIndex());
                    String color = rowItem.getSubjectColor();
                    if (color != null && !color.isEmpty()) {
                        if (!color.startsWith("#")) color = "#" + color;
                        setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Styling for Type column
        colType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("EXAM")) {
                        setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-alignment: CENTER; -fx-font-weight: bold; -fx-background-radius: 5;");
                    } else {
                        setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-alignment: CENTER; -fx-font-weight: bold; -fx-background-radius: 5;");
                    }
                }
            }
        });
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
            case "1 Week" -> 7;
            case "2 Weeks" -> 14;
            case "3 Weeks" -> 21;
            case "1 Month" -> 30;
            default -> 7;
        };

        LocalDate limitDate = LocalDate.now().plusDays(days);
        LocalDate today = LocalDate.now();

        List<DashboardItem> activities = new ArrayList<>();

        // Add Tasks
        for (Task t : allTasks) {
            if ((t.getIsCompleted() == null || !t.getIsCompleted()) && 
                t.getDueDate() != null && !t.getDueDate().isBefore(today) && !t.getDueDate().isAfter(limitDate)) {
                activities.add(new DashboardItem("TASK", t.getTitle(), t.getDueDate(), t.getSubjectName(), t.getSubjectColor(), t.getIsCompleted()));
            }
        }

        // Add Exams
        for (Exam e : allExams) {
            if (e.getExamDate() != null && !e.getExamDate().isBefore(today) && !e.getExamDate().isAfter(limitDate)) {
                activities.add(new DashboardItem("EXAM", e.getNameExam() + " (" + e.getExamType() + ")", e.getExamDate(), e.getSubjectName(), e.getSubjectColor(), false));
            }
        }

        // Sort by Date
        activities.sort(Comparator.comparing(DashboardItem::getDate));

        activityTable.setItems(FXCollections.observableArrayList(activities));
    }

    private void loadStatistics() {
        try {
            long pendingTasks = allTasks.stream()
                    .filter(t -> t.getIsCompleted() != null && !t.getIsCompleted())
                    .count();
            lblTasksCount.setText(pendingTasks + " Pending Tasks");

            if (!allExams.isEmpty()) {
                lblNextExam.setText("Next: " + allExams.get(0).getNameExam());
            } else {
                lblNextExam.setText("No exams scheduled");
            }

            List<Grade> grades = gradeService.getAllGrades();
            if (!grades.isEmpty()) {
                double avg = grades.stream().mapToDouble(Grade::getScore).average().orElse(0.0);
                lblGPA.setText(String.format("Overall GPA: %.2f", avg));
            } else {
                lblGPA.setText("No grades yet");
            }
        } catch (Exception e) {
            lblTasksCount.setText("Error loading data");
        }
    }
}