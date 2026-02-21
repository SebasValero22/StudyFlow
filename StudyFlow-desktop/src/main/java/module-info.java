module com.iescamp.studyflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires static lombok;
    requires java.net.http;
    requires java.desktop;
    
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.annotation;

    opens com.iescamp.studyflow to javafx.fxml;
    exports com.iescamp.studyflow;
    opens com.iescamp.studyflow.controller to javafx.fxml;
    exports com.iescamp.studyflow.controller;
    
    // Allow Jackson to access model classes
    opens com.iescamp.studyflow.model to com.fasterxml.jackson.databind;
    exports com.iescamp.studyflow.model;
}
