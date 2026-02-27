package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Subject;
import com.iescamp.studyflow.service.SubjectService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

import com.iescamp.studyflow.model.Exam;
import com.iescamp.studyflow.model.Grade;
import com.iescamp.studyflow.model.Task;
import com.iescamp.studyflow.service.ExamService;
import com.iescamp.studyflow.service.GradeService;
import com.iescamp.studyflow.service.TaskService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class SubjectsController {

    @FXML private TableView<Subject> subjectsTable;
    @FXML private TableColumn<Subject, String> nameColumn;
    @FXML private Label statusLabel;

    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> colTaskTitle;
    @FXML private TableColumn<Task, LocalDate> colTaskDate;

    @FXML private TableView<Exam> examsTable;
    @FXML private TableColumn<Exam, String> colExamName;
    @FXML private TableColumn<Exam, LocalDate> colExamDate;

    @FXML private TableView<Grade> gradesTable;
    @FXML private TableColumn<Grade, String> colGradeConcept;
    @FXML private TableColumn<Grade, Double> colGradeScore;

    private final SubjectService subjectService = new SubjectService();
    private final TaskService taskService = new TaskService();
    private final ExamService examService = new ExamService();
    private final GradeService gradeService = new GradeService();

    @FXML
    public void initialize() {
        setupTable();
        setupDetailTables();
        loadSubjects();

        // Add selection listener
        subjectsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadSubjectDetails(newSelection);
            }
        });
    }

    private void setupTable() {
        nameColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Subject, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Subject subject = getTableView().getItems().get(getIndex());
                    String color = subject.getColor();
                    if (color != null && !color.isEmpty()) {
                        if (!color.startsWith("#")) color = "#" + color;
                        setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void setupDetailTables() {
        colTaskTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTaskDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        colExamName.setCellValueFactory(new PropertyValueFactory<>("nameExam"));
        colExamDate.setCellValueFactory(new PropertyValueFactory<>("examDate"));

        colGradeConcept.setCellValueFactory(new PropertyValueFactory<>("concept"));
        colGradeScore.setCellValueFactory(new PropertyValueFactory<>("score"));
    }

    private void loadSubjectDetails(Subject subject) {
        try {
            int sid = subject.getSubjectId();

            // Filter Tasks
            List<Task> allTasks = taskService.getAllTasks();
            List<Task> filteredTasks = allTasks.stream()
                    .filter(t -> t.getSubjectId() != null && t.getSubjectId() == sid)
                    .collect(Collectors.toList());
            tasksTable.setItems(FXCollections.observableArrayList(filteredTasks));

            // Filter Exams
            List<Exam> allExams = examService.getAllExams();
            List<Exam> filteredExams = allExams.stream()
                    .filter(e -> e.getSubjectId() == sid)
                    .collect(Collectors.toList());
            examsTable.setItems(FXCollections.observableArrayList(filteredExams));

            // Filter Grades
            List<Grade> allGrades = gradeService.getAllGrades();
            List<Grade> filteredGrades = allGrades.stream()
                    .filter(g -> g.getSubjectId() == sid)
                    .collect(Collectors.toList());
            gradesTable.setItems(FXCollections.observableArrayList(filteredGrades));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSubjects() {
        try {
            List<Subject> list = subjectService.getAllSubjects();
            subjectsTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            statusLabel.setText("Error loading subjects: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCreateSubject(ActionEvent event) {
        try {
            // Cargar el formulario que YA tienes creado
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iescamp/studyflow/fxml/subject_form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Subject");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Al cerrar la ventana, recargamos la tabla para ver la nueva
            loadSubjects();

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error opening form.");
        }
    }

    @FXML
    public void handleEditSubject(ActionEvent event) {
        Subject selected = subjectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a subject to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iescamp/studyflow/fxml/subject_form.fxml"));
            Parent root = loader.load();

            SubjectFormController controller = loader.getController();
            controller.setSubjectToEdit(selected);

            Stage stage = new Stage();
            stage.setTitle("Edit Subject");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadSubjects();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error opening edit form.");
        }
    }

    @FXML
    public void handleDeleteSubject(ActionEvent event) {
        Subject selected = subjectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a subject first.");
            return;
        }

        try {
            // Llamada al servicio para borrar
            subjectService.deleteSubject(selected.getSubjectId());
            loadSubjects(); // Refrescar tabla
            statusLabel.setText("Subject deleted.");
        } catch (Exception e) {
            statusLabel.setText("Error deleting: " + e.getMessage());
        }
    }
}