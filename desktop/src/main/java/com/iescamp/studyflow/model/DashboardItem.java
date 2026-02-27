package com.iescamp.studyflow.model;

import java.time.LocalDate;

public class DashboardItem {
    private String type; // "TASK" or "EXAM"
    private String title;
    private LocalDate date;
    private String subjectName;
    private String subjectColor;
    private Boolean completed;

    public DashboardItem(String type, String title, LocalDate date, String subjectName, String subjectColor, Boolean completed) {
        this.type = type;
        this.title = title;
        this.date = date;
        this.subjectName = subjectName;
        this.subjectColor = subjectColor;
        this.completed = completed;
    }

    // Getters
    public String getType() { return type; }
    public String getTitle() { return title; }
    public LocalDate getDate() { return date; }
    public String getSubjectName() { return subjectName; }
    public String getSubjectColor() { return subjectColor; }
    public Boolean getCompleted() { return completed; }
}
