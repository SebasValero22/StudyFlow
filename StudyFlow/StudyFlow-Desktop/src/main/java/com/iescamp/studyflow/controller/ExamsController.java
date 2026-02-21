package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Exam;
import com.iescamp.studyflow.service.ExamService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ExamsController {
    @FXML private TableView<Exam> examsTable;
    @FXML private TableColumn<Exam, String> colExamName;
    @FXML private TableColumn<Exam, String> colExamType;
    @FXML private TableColumn<Exam, String> colDate;
    @FXML private TableColumn<Exam, String> colRoom;

    private final ExamService examService = new ExamService();

    @FXML
    public void initialize() {
        // 1. Primero configuramos las columnas (esto nunca debe fallar)
        colExamName.setCellValueFactory(new PropertyValueFactory<>("nameExam"));
        colExamType.setCellValueFactory(new PropertyValueFactory<>("examType"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("examDate"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("classroom"));

        // 2. Cargamos los datos de forma segura
        try {
            loadExams();
        } catch (Exception e) {
            // En lugar de dejar que la app explote, mostramos un error en consola o UI
            System.err.println("No se pudieron cargar los exámenes: " + e.getMessage());
            // Opcional: mostrar una alerta al usuario
        }
    }

    private void loadExams() throws Exception {
        examsTable.setItems(FXCollections.observableArrayList(examService.getAllExams()));
    }

    @FXML private void handleNewExam() { /* Lógica para abrir form de examen */ }
}