module com.iescamp.studyflow {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires java.sql;
    requires static lombok;
    requires java.net.http;
    requires java.desktop;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.datatype.jsr310;

    opens com.iescamp.studyflow to javafx.fxml;
    exports com.iescamp.studyflow;
    opens com.iescamp.studyflow.controller to javafx.fxml;
    exports com.iescamp.studyflow.controller;
    opens com.iescamp.studyflow.model to com.fasterxml.jackson.databind, com.fasterxml.jackson.datatype.jsr310;
    exports com.iescamp.studyflow.model;
}
