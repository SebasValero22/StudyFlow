package com.iescamp.studyflow.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainDashboardController {
    @FXML public StackPane contentArea;
    @FXML public Label statusLabel;

    @FXML
    public void initialize() {
        // Cargar la vista por defecto al iniciar
        loadView("tasks_overview.fxml");
        statusLabel.setText("Bienvenido a StudyFlow");
    }

    public void showOverview(ActionEvent actionEvent) {
        loadView("overview_view.fxml"); // Asumiendo que existe
        statusLabel.setText("Viendo: Resumen General");
    }

    public void showTasks(ActionEvent actionEvent) {
        loadView("tasks_overview.fxml");
        statusLabel.setText("Viendo: Mis Tareas");
    }

    public void showSubjects(ActionEvent actionEvent) {
        loadView("subjects_view.fxml"); // Tendrás que crear este FXML
        statusLabel.setText("Viendo: Asignaturas");
    }

    public void showExams(ActionEvent actionEvent) {
        // loadView("exams_view.fxml");
        statusLabel.setText("Sección Exámenes (Próximamente)");
    }

    public void showGrades(ActionEvent actionEvent) {
        // loadView("grades_view.fxml");
        statusLabel.setText("Sección Notas (Próximamente)");
    }

    // Método auxiliar para cambiar el contenido del centro
    private void loadView(String fxmlFile) {
        try {
            // ERROR AQUÍ: Antes tenías "/view/", cámbialo a "/fxml/"
            String path = "/com/iescamp/studyflow/fxml/" + fxmlFile;

            // Verificación de seguridad para que no te vuelva a pasar
            if (getClass().getResource(path) == null) {
                System.err.println("¡ERROR! No encuentro el archivo: " + path);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error al cargar la vista: " + fxmlFile);
        }
    }
}