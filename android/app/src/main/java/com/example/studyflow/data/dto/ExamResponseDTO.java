package com.example.studyflow.data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExamResponseDTO {
    private Integer examId;
    private String nameExam;
    private String classroom;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate examDate;
    
    private String examType; // e.g., PARCIAL, FINAL
    private Integer subjectId;
    private String subjectName;
    private String subjectColor;

    public Integer getExamId() { return examId; }
    public void setExamId(Integer examId) { this.examId = examId; }
    public String getNameExam() { return nameExam; }
    public void setNameExam(String nameExam) { this.nameExam = nameExam; }
    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }
    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getSubjectColor() { return subjectColor; }
    public void setSubjectColor(String subjectColor) { this.subjectColor = subjectColor; }
}
