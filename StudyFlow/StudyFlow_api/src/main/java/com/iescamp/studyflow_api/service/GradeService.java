package com.iescamp.studyflow_api.service;


import com.iescamp.studyflow_api.dto.GradeDTO;
import com.iescamp.studyflow_api.model.Grade;
import com.iescamp.studyflow_api.model.Reminder;
import com.iescamp.studyflow_api.repository.GradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GradeService {
    @Autowired
    private GradeRepository gradeRepository;


    public GradeDTO add(GradeDTO dto){
        Grade grade = new Grade();
        grade.setConcept(dto.getConcept());
        grade.setScore(dto.getScore());
        grade.setSubject(dto.getSubject());
        grade.setWeight(dto.getWeight());
        grade.setDate(dto.getDate());
        return GradeDTO.convertToDTO(gradeRepository.save(grade));

    }
    public void delete(Integer id){
        gradeRepository.deleteById(id);
        throw new RuntimeException("Grade deleted succesfully");
    }

    public GradeDTO modify(Integer id, GradeDTO dto){
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grade not found"));

        grade.setConcept(dto.getConcept());
        grade.setScore(dto.getScore()); // Must be 0-10
        grade.setWeight(dto.getWeight());

        return GradeDTO.convertToDTO(gradeRepository.save(grade)); // Don't forget .save()!
    }

    public List<GradeDTO> findAll(){
        List<Grade> grades = gradeRepository.findAll();
        return grades.stream().map(GradeDTO::convertToDTO).toList();
    }


    public GradeDTO findById(Integer id){
        Optional<Grade> grades = gradeRepository.findById(id);
        return GradeDTO.convertToDTO(grades.orElseThrow(()-> new RuntimeException("no existe id")));
    }

}
