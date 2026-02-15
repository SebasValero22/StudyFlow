package com.iescamp.studyflow_api.service;

import com.iescamp.studyflow_api.dto.ExamDTO;
import com.iescamp.studyflow_api.model.Exam;
import com.iescamp.studyflow_api.repository.ExamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ExamService {

    @Autowired
    private ExamRepository examRepository;


    public ExamDTO add(ExamDTO dto){
        Exam exam = new Exam();
        exam.setExamId(dto.getExamId());
        exam.setName(dto.getName());
        exam.setExamType(dto.getExamType());
        exam.setExamDate(dto.getExamDate());
        exam.setClassroom(dto.getClassroom());
        exam.setSubject(dto.getSubject());

        return ExamDTO.convertToDTO(examRepository.save(exam));
    }

    public void delete_(Integer id){
        if (!examRepository.existsById(id)) throw new RuntimeException("exam not found");
        examRepository.deleteById(id);
    }

    public ExamDTO modify(Integer id, ExamDTO dto){
        // 1. Find existing
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        // 2. Update fields
        exam.setName(dto.getName());
        exam.setExamType(dto.getExamType());
        exam.setExamDate(dto.getExamDate());
        exam.setClassroom(dto.getClassroom());

        // 3. Save the managed object
        return ExamDTO.convertToDTO(examRepository.save(exam));
    }

    public List<ExamDTO> findAll(){
        return examRepository.findAll().stream()
                .map(ExamDTO::convertToDTO)
                .collect(Collectors.toList());
    }


    public List<ExamDTO> findByName(String name){
        List<Exam> exams = examRepository.findBySubject_NameSubject(name);
        if (exams.isEmpty()){
            throw new RuntimeException("no exams found"+name);
        }
        return exams.stream()
                .map(ExamDTO::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ExamDTO> findBySubject(String subjectName){
        // ¡Usamos el nuevo nombre con guion bajo!
        List<Exam> exams = examRepository.findBySubject_NameSubject(subjectName);

        if (exams.isEmpty()) {
            throw new RuntimeException("no exams found with that subject: " + subjectName);
        }
        return exams.stream()
                .map(ExamDTO::convertToDTO)
                .toList();
    }


}
