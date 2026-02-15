module com.iescamp.studyflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires static lombok;
    requires java.net.http;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;

    opens com.iescamp.studyflow to javafx.fxml;
    exports com.iescamp.studyflow;
    opens com.iescamp.studyflow.controller to javafx.fxml;
    exports com.iescamp.studyflow.controller;
    opens com.iescamp.studyflow.model to com.fasterxml.jackson.databind;
    exports com.iescamp.studyflow.model;
}
