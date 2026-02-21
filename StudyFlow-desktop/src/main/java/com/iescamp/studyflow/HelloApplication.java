package com.iescamp.studyflow;

import com.iescamp.studyflow.utils.ViewSwitcher;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        ViewSwitcher.setStage(stage); // we give it a switcher based on "viewSwitcher"
        stage.setTitle("StudyFlow - Academic Manager"); //title
        ViewSwitcher.loadView("login_view.fxml"); // load the view

        // 4. Show the window
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}