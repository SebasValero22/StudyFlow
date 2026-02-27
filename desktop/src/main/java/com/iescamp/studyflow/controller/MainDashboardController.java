package com.iescamp.studyflow.controller;

import javafx.event.ActionEvent;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainDashboardController {
    @FXML public StackPane contentArea;
    @FXML public Label statusLabel;
    @FXML private VBox sidebarMenu; // Asegúrate de añadir fx:id="sidebarMenu" en el VBox del FXML

    @FXML
    public void initialize() {
            showOverview(null);
            statusLabel.setText("Bienvenido a StudyFlow");
    }

    @FXML
    public void showOverview(Event event) {
        loadView("overview_view.fxml");
        updateUI(event, "Viendo: Resumen General");
    }

    @FXML
    public void showTasks(ActionEvent actionEvent) {
        loadView("tasks_overview.fxml");
        updateUI(actionEvent, "Viendo: Mis Tareas");
    }

    @FXML
    public void showSubjects(ActionEvent actionEvent) {
        loadView("subjects_view.fxml");
        updateUI(actionEvent, "Viendo: Asignaturas");
    }

    @FXML
    public void showExams(ActionEvent actionEvent) {
        loadView("exams_view.fxml");
        updateUI(actionEvent, "Viendo: Próximos Exámenes");
    }

    @FXML
    public void showGrades(ActionEvent actionEvent) {
        loadView("grades_view.fxml");
        updateUI(actionEvent, "Viendo: Calificaciones Académicas");
    }

    @FXML
    private void loadView(String fxmlFile) {
        try {
            // CORRECCIÓN: Usa la variable fxmlFile, no pongas "register_view.fxml" a mano
            String path = "/com/iescamp/studyflow/fxml/" + fxmlFile;

            if (getClass().getResource(path) == null) {
                // Este es el error que ves ahora, porque overview_view.fxml no existía
                System.err.println("¡ERROR! No se encuentra el archivo FXML en: " + path);
                statusLabel.setText("Error: Archivo " + fxmlFile + " no encontrado.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error crítico al cargar: " + fxmlFile);
        }
    }

    // Método auxiliar para limpiar el estilo de los botones y actualizar el label
    private void updateUI(Event event, String statusText) {
        statusLabel.setText(statusText);

        // Solo intentamos resaltar si el origen es realmente un botón
        if (event != null && event.getSource() instanceof Button) {
            Button btn = (Button) event.getSource();
            // Tu lógica de estilos aquí...
        }
    }

    @FXML
    public void showUserConfig(ActionEvent actionEvent) {
        loadView("user_config_view.fxml"); // Asegúrate de que el nombre coincida con tu archivo
        statusLabel.setText("Configuración de Perfil");
    }
}