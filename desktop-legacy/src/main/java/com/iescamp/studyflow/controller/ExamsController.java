package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Exam;
import com.iescamp.studyflow.service.ExamService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class ExamsController {
    @FXML private TableView<Exam> examsTable;
    @FXML private TableColumn<Exam, String> colExamName;
    @FXML private TableColumn<Exam, String> colExamType;
    @FXML private TableColumn<Exam, String> colDate;
    @FXML private TableColumn<Exam, String> colRoom;

    private final ExamService examService = new ExamService();

    @FXML
    public void initialize() {
        // ... (setupTable code) ...
        colExamName.setCellValueFactory(new PropertyValueFactory<>("nameExam"));
        colExamType.setCellValueFactory(new PropertyValueFactory<>("examType"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("examDate"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("classroom"));

        colExamName.setCellFactory(column -> new TableCell<Exam, String>() {
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
                    if (color != null && !color.isEmpty()) {
                        if (!color.startsWith("#")) color = "#" + color;
                        setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        loadExamsSilently();
    }

    private void loadExamsSilently() {
        try {
            loadExams();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadExams() throws Exception {
        examsTable.setItems(FXCollections.observableArrayList(examService.getAllExams()));
    }

    @FXML 
    private void handleNewExam() { 
        openExamForm(null);
    }

    @FXML
    private void handleEditExam() {
        Exam selected = examsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        openExamForm(selected);
    }

    @FXML
    private void handleDeleteExam() {
        Exam selected = examsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            examService.deleteExam(selected.getExamId());
            loadExams();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openExamForm(Exam exam) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iescamp/studyflow/fxml/exam_form.fxml"));
            Parent root = loader.load();

            if (exam != null) {
                ExamFormController controller = loader.getController();
                controller.setExamToEdit(exam);
            }

            Stage stage = new Stage();
            stage.setTitle(exam == null ? "Nuevo Examen" : "Editar Examen");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadExams();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}