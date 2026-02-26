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
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class SubjectsController {

    @FXML private TableView<Subject> subjectsTable;
    @FXML private Label statusLabel;

    private final SubjectService subjectService = new SubjectService();

    @FXML
    public void initialize() {
        loadSubjects();
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
        openForm(null);
    }

    @FXML
    public void handleEditSubject(ActionEvent event) {
        Subject selected = subjectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a subject to modify.");
            return;
        }
        openForm(selected);
    }

    private void openForm(Subject subject) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iescamp/studyflow/fxml/subject_form.fxml"));
            Parent root = loader.load();

            if (subject != null) {
                SubjectFormController controller = loader.getController();
                controller.setSubject(subject); 
            }

            Stage stage = new Stage();
            stage.setTitle(subject == null ? "New Subject" : "Modify Subject");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadSubjects();

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error opening form.");
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
            // Call service to delete
            subjectService.deleteSubject(selected.getSubjectId());
            loadSubjects(); // Refresh table
            statusLabel.setText("Subject deleted.");
        } catch (Exception e) {
            statusLabel.setText("Error deleting: " + e.getMessage());
        }
    }
}