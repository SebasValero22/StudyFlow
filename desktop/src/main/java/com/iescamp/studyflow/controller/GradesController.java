package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Grade;
import com.iescamp.studyflow.service.GradeService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class GradesController {
    @FXML private TableView<Grade> gradesTable;
    @FXML private TableColumn<Grade, String> colConcept;
    @FXML private TableColumn<Grade, Double> colScore;
    @FXML private TableColumn<Grade, Double> colWeight;
    @FXML private TableColumn<Grade, String> colDate;
    @FXML private Label lblAverage;

    private final GradeService gradeService = new GradeService();

    @FXML
    public void initialize() {
        // Enlazamos con los campos de la tabla Grades del SQL
        colConcept.setCellValueFactory(new PropertyValueFactory<>("concept"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("gradeDate"));

        loadGrades();
    }

    private void loadGrades() {
        try {
            List<Grade> grades = gradeService.getAllGrades();
            gradesTable.setItems(FXCollections.observableArrayList(grades));
            calculateAverage(grades);
        } catch (Exception e) {
            lblAverage.setText("Error al conectar con la API");
            e.printStackTrace();
        }
    }

    private void calculateAverage(List<Grade> grades) {
        if (grades.isEmpty()) return;
        double weightedSum = 0;
        for (Grade g : grades) {
            weightedSum += (g.getScore() * (g.getWeight() / 100));
        }
        lblAverage.setText(String.format("Media Ponderada: %.2f", weightedSum));
    }
}