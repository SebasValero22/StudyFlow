package com.iescamp.studyflow_api.dto;

import com.iescamp.studyflow_api.model.Grade;
import com.iescamp.studyflow_api.model.Subject;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.util.Date;

@Data
public class GradeDTO {

    private Integer gradeId;
    private Subject subject;
    private String concept;
    private Double score;
    private Double weight;
    private Date date;



    public static GradeDTO convertToDTO(Grade grade){
        GradeDTO dto = new GradeDTO();

        dto.setGradeId(grade.getGradeId());
        dto.setConcept(grade.getConcept());
        dto.setSubject(grade.getSubject());
        dto.setWeight(grade.getWeight());
        dto.setScore(grade.getScore());
        dto.setDate(grade.getDate());



        return dto;
    }





}
