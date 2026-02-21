package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Exam;
import com.iescamp.studyflow.service.ExamService;
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

public class ExamsController {
    @FXML private TableView<Exam> examsTable;
    @FXML private TableColumn<Exam, Void> colDone;
    @FXML private TableColumn<Exam, String> colRemaining;
    @FXML private TableColumn<Exam, String> colExamName;
    @FXML private TableColumn<Exam, String> colExamType;
    @FXML private TableColumn<Exam, String> colDate;
    @FXML private TableColumn<Exam, String> colRoom;

    private final ExamService examService = new ExamService();
    private final com.iescamp.studyflow.service.SubjectService subjectService = new com.iescamp.studyflow.service.SubjectService();
    private java.util.Map<Integer, String> subjectColors = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        loadSubjectColors();

        colDone.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
            private final javafx.scene.control.Button btn = new javafx.scene.control.Button();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Exam exam = getTableView().getItems().get(getIndex());
                    boolean completed = exam.getIsCompleted() != null && exam.getIsCompleted();
                    btn.setText(completed ? "TRUE" : "FALSE");
                    btn.setStyle("-fx-background-color: " + (completed ? "#2ecc71" : "#bdc3c7") + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
                    btn.setOnAction(event -> toggleExamStatus(exam));
                    setGraphic(btn);
                }
            }
        });

        examsTable.setRowFactory(tv -> new javafx.scene.control.TableRow<>() {
            @Override
            protected void updateItem(Exam item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    String color = subjectColors.getOrDefault(item.getSubjectId(), "#ffffff");
                    setStyle("-fx-background-color: " + color + "33; -fx-border-color: " + color + "; -fx-border-width: 0 0 0 5;");
                }
            }
        });

        colExamName.setCellValueFactory(new PropertyValueFactory<>("nameExam"));
        colExamType.setCellValueFactory(new PropertyValueFactory<>("examType"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("examDate"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("classroom"));
        
        if (colRemaining != null) {
            colRemaining.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleStringProperty(data.getValue().getDaysRemaining()));
        }

        try {
            loadExams();
        } catch (Exception e) {
            System.err.println("Error loading exams.");
        }
    }

    private void loadExams() throws Exception {
        examsTable.setItems(FXCollections.observableArrayList(examService.getAllExams()));
    }

    private void loadSubjectColors() {
        try {
            subjectService.getAllSubjects().forEach(s -> subjectColors.put(s.getSubjectId(), s.getColor()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void handleNewExam() {
        openForm(null);
    }

    @FXML
    public void handleEditExam() {
        Exam selected = examsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        openForm(selected);
    }

    private void openForm(Exam exam) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iescamp/studyflow/fxml/exam_form.fxml"));
            Parent root = loader.load();

            if (exam != null) {
                ExamFormController controller = loader.getController();
                controller.setExam(exam);
            }

            Stage stage = new Stage();
            stage.setTitle(exam == null ? "New Exam" : "Modify Exam");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadExams();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void toggleExamStatus(Exam exam) {
        try {
            exam.setIsCompleted(!exam.getIsCompleted());
            examService.updateExam(exam.getExamId(), exam);
            loadExams();
        } catch (Exception e) {
            System.err.println("Could not update exam status.");
        }
    }
}
