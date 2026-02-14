package com.iescamp.studyflow.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ViewSwitcher {
    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadView(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(ViewSwitcher.class.getResource("/fxml/" + fxmlPath));
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + fxmlPath);
            e.printStackTrace(); // En una app real, mostrar mensaje al usuario [cite: 211, 467]
        }
    }
}