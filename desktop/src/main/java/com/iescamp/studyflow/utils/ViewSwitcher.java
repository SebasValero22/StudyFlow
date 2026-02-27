package com.iescamp.studyflow.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class ViewSwitcher {
    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadView(String fxmlFile) {
        try {

            String path = "/com/iescamp/studyflow/fxml/" + fxmlFile;
            URL resource = ViewSwitcher.class.getResource(path);
            // we see if the root is failing
            if (resource == null) {
                throw new RuntimeException("cannot find the fxml in : " + path);
            }

            Parent root = FXMLLoader.load(resource);

            // Optimización: Si ya existe una escena, solo cambiamos la raíz (es más rápido y suave)
            if (primaryStage.getScene() == null) {
                primaryStage.setScene(new Scene(root));
            } else {
                primaryStage.getScene().setRoot(root);
            }

            primaryStage.sizeToScene(); // Ajusta el tamaño de la ventana al nuevo contenido
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("couldnt load the view :(: " + fxmlFile);
            e.printStackTrace();
        }
    }
}