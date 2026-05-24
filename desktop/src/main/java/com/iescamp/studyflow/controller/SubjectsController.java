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
    @FXML private TableColumn<Subject, String> colorColumn;
    @FXML private TableColumn<Subject, Void> actionsColumn;
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

        // Listener de seleccion
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
                        // Usamos el color como borde y un texto oscuro/negrita para maxima legibilidad
                        setStyle("-fx-border-color: " + color + "; -fx-border-width: 0 0 0 5; -fx-padding: 0 0 0 10; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                        setText(item);
                    } else {
                        setStyle("");
                        setText(item);
                    }
                }
            }
        });

        colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        colorColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Subject, String>() {
            @Override
            protected void updateItem(String color, boolean empty) {
                super.updateItem(color, empty);
                if (empty || color == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String hexColor = color.startsWith("#") ? color : "#" + color;
                    javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(8, javafx.scene.paint.Color.web(hexColor));
                    Label hexLabel = new Label(hexColor);
                    javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(8, circle, hexLabel);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });

        actionsColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Subject, Void>() {
            private final Button btnEdit = new Button("Modify");
            private final Button btnDelete = new Button("Delete");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                
                btnEdit.setOnAction(event -> {
                    Subject subject = getTableView().getItems().get(getIndex());
                    subjectsTable.getSelectionModel().select(subject);
                    handleEditSubject(null);
                });

                btnDelete.setOnAction(event -> {
                    Subject subject = getTableView().getItems().get(getIndex());
                    subjectsTable.getSelectionModel().select(subject);
                    handleDeleteSubject(null);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
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

            // Filtrar tareas
            List<Task> allTasks = taskService.getAllTasks();
            List<Task> filteredTasks = allTasks.stream()
                    .filter(t -> t.getSubjectId() != null && t.getSubjectId() == sid)
                    .collect(Collectors.toList());
            tasksTable.setItems(FXCollections.observableArrayList(filteredTasks));

            // Filtrar examenes
            List<Exam> allExams = examService.getAllExams();
            List<Exam> filteredExams = allExams.stream()
                    .filter(e -> e.getSubjectId() == sid)
                    .collect(Collectors.toList());
            examsTable.setItems(FXCollections.observableArrayList(filteredExams));

            // Filtrar notas
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
            // Cargar el formulario
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
