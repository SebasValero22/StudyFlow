package com.iescamp.studyflow_api.service;


import com.iescamp.studyflow_api.dto.SubjectResponseDTO;
import com.iescamp.studyflow_api.model.Subject;
import com.iescamp.studyflow_api.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubjectService {

        @Autowired
        private SubjectRepository subjectRepository;

     public SubjectResponseDTO add(SubjectResponseDTO dto) {
         Subject subject = new Subject();
         subject.setNameSubject(dto.getNameSubject());
         subject.setColor(dto.getColor());
         subject.setActiveSubject(dto.getActiveSubject());
         subject.setAcademicYear(dto.getAcademicYear());
         // Necesario para guardar el userId; sin esto se queda nulo.
         subject.setUserId(dto.getUserId());
         return SubjectResponseDTO.convertToDTO(subjectRepository.save(subject));
     }

        public SubjectResponseDTO modify(Integer id, SubjectResponseDTO dto) {
            Subject subject = subjectRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Subject not found"));
            subject.setNameSubject(dto.getNameSubject());
            subject.setColor(dto.getColor());
            subject.setActiveSubject(dto.getActiveSubject());
            subject.setAcademicYear(dto.getAcademicYear());
            return SubjectResponseDTO.convertToDTO(subjectRepository.save(subject));
        }

    public List<SubjectResponseDTO> findAll(Integer userId) {
        if (userId != null) {
            return subjectRepository.findByUserId(userId).stream()
                    .map(SubjectResponseDTO::convertToDTO)
                    .collect(Collectors.toList());
        }
        return subjectRepository.findAll().stream()
                .map(SubjectResponseDTO::convertToDTO)
                .collect(Collectors.toList());
    }

        public void delete(Integer id) {
            if (!subjectRepository.existsById(id)) throw new RuntimeException("Subject not found");
            subjectRepository.deleteById(id);
        }

     public SubjectResponseDTO findById(Integer id) {
         Subject subject = subjectRepository.findById(id)
                 .orElseThrow(() -> new RuntimeException("Subject not found with ID: " + id));

         return SubjectResponseDTO.convertToDTO(subject);
     }

     public List<SubjectResponseDTO> findByName(String name) {
         List<Subject> subjects = subjectRepository.findByNameSubject(name);

         if (subjects.isEmpty()) {
             throw new RuntimeException("No subjects found with name: " + name);
         }

         return subjects.stream()
                 .map(SubjectResponseDTO::convertToDTO)
                 .collect(Collectors.toList());
     }
 }
