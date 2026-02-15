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
            statusLabel.setText("Error cargando asignaturas: " + e.getMessage());
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
            stage.setTitle("Nueva Asignatura");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Al cerrar la ventana, recargamos la tabla para ver la nueva
            loadSubjects();

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error al abrir el formulario.");
        }
    }

    @FXML
    public void handleDeleteSubject(ActionEvent event) {
        Subject selected = subjectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Selecciona una asignatura primero.");
            return;
        }

        try {
            // Llamada al servicio para borrar
            subjectService.deleteSubject(selected.getSubjectId());
            loadSubjects(); // Refrescar tabla
            statusLabel.setText("Asignatura eliminada.");
        } catch (Exception e) {
            statusLabel.setText("Error al eliminar: " + e.getMessage());
        }
    }
}