module com.iescamp.studyflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.iescamp.studyflow to javafx.fxml;
    exports com.iescamp.studyflow;
    exports com.iescamp.studyflow.CONTROLLERS;
    opens com.iescamp.studyflow.CONTROLLERS to javafx.fxml;
}