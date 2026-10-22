module com.iescamp.studyflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.prefs;
    requires java.sql;
    requires static lombok;
    requires java.net.http;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.github.kwhat.jnativehook;

    opens com.iescamp.studyflow to javafx.fxml;
    exports com.iescamp.studyflow;
    opens com.iescamp.studyflow.controller to javafx.fxml;
    exports com.iescamp.studyflow.controller;
    opens com.iescamp.studyflow.model to com.fasterxml.jackson.databind;
    exports com.iescamp.studyflow.model;
    opens com.iescamp.studyflow.service to javafx.fxml, com.fasterxml.jackson.databind;
    exports com.iescamp.studyflow.service;
    opens com.iescamp.studyflow.database to javafx.fxml;
    exports com.iescamp.studyflow.database;
    opens com.iescamp.studyflow.utils to javafx.fxml;
    exports com.iescamp.studyflow.utils;
}
