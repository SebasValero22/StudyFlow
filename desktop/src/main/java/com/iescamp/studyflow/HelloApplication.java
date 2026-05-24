package com.iescamp.studyflow;

import com.iescamp.studyflow.utils.ViewSwitcher;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        ViewSwitcher.setStage(stage);
        stage.setTitle("StudyFlow - Academic Manager");
        ViewSwitcher.loadView("login_view.fxml");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
