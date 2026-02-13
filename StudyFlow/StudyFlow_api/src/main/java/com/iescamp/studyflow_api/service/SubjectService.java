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
            subject.setName(dto.getSubjectName());
            subject.setColor(dto.getColor());
            subject.setActive(dto.isActive());
            subject.setAcademicYear(dto.getAcademicYear());
            return SubjectResponseDTO.convertToDTO(subjectRepository.save(subject));
        }

        public SubjectResponseDTO modify(Integer id, SubjectResponseDTO dto) {
            Subject subject = subjectRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Subject not found"));
            subject.setName(dto.getSubjectName());
            subject.setColor(dto.getColor());
            subject.setActive(dto.isActive());
            subject.setAcademicYear(dto.getAcademicYear());
            return SubjectResponseDTO.convertToDTO(subjectRepository.save(subject));
        }

        public List<SubjectResponseDTO> findAll() {
            return subjectRepository.findAll().stream()
                    .map(SubjectResponseDTO::convertToDTO)
                    .collect(Collectors.toList());
        }

        public void delete(Integer id) {
            if (!subjectRepository.existsById(id)) throw new RuntimeException("Subject not found");
            subjectRepository.deleteById(id);
        }

    public SubjectResponseDTO findById(Integer id) {
        // 1. Use the repository to find a single Optional result
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with ID: " + id));

        // 2. Convert that single entity into a DTO
        return SubjectResponseDTO.convertToDTO(subject);
    }

    public List<SubjectResponseDTO> findByName(String name) {
        // 1. Get the list of all matching entities from the repo
        List<Subject> subjects = subjectRepository.findByName(name);

        // 2. Check if the list is empty to handle the "not found" case
        if (subjects.isEmpty()) {
            throw new RuntimeException("No subjects found with name: " + name);
        }

        // 3. Transform the list of entities into a list of DTOs
        return subjects.stream()
                .map(SubjectResponseDTO::convertToDTO)
                .collect(Collectors.toList());
    }
}
