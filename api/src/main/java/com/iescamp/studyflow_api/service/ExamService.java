package com.iescamp.studyflow_api.service;

import com.iescamp.studyflow_api.dto.ExamDTO;
import com.iescamp.studyflow_api.model.Exam;
import com.iescamp.studyflow_api.model.LifeArea;
import com.iescamp.studyflow_api.repository.ExamRepository;
import com.iescamp.studyflow_api.repository.LifeAreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ExamService {

    @Autowired
    private ExamRepository examRepository;
    // Importa el repositorio de asignaturas
    @Autowired
    private LifeAreaRepository LifeAreaRepository;

    public ExamDTO add(ExamDTO dto) {
        Exam exam = new Exam();
        exam.setExamId(dto.getExamId());
        exam.setName(dto.getNameExam());
        exam.setExamType(dto.getExamType());
        exam.setExamDate(dto.getExamDate());
        exam.setClassroom(dto.getClassroom());

        // BUSCA LA ASIGNATURA REAL POR ID
        if (dto.getLifeAreaId() != null) {
            LifeArea LifeArea = LifeAreaRepository.findById(dto.getLifeAreaId())
                    .orElseThrow(() -> new RuntimeException("LifeArea not found with ID: " + dto.getLifeAreaId()));
            exam.setLifeArea(LifeArea); // Ahora sí pasamos el objeto LifeArea completo
        }

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
        exam.setName(dto.getNameExam());
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
        List<Exam> exams = examRepository.findByLifeArea_name(name);
        if (exams.isEmpty()){
            throw new RuntimeException("no exams found"+name);
        }
        return exams.stream()
                .map(ExamDTO::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ExamDTO> findByLifeArea(String LifeAreaName){
        // ¡Usamos el nuevo nombre con guion bajo!
        List<Exam> exams = examRepository.findByLifeArea_name(LifeAreaName);

        if (exams.isEmpty()) {
            throw new RuntimeException("no exams found with that LifeArea: " + LifeAreaName);
        }
        return exams.stream()
                .map(ExamDTO::convertToDTO)
                .toList();
    }


}
