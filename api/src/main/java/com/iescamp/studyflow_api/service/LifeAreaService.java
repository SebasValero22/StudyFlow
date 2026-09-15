package com.iescamp.studyflow_api.service;

import com.iescamp.studyflow_api.dto.LifeAreaResponseDTO;
import com.iescamp.studyflow_api.model.LifeArea;
import com.iescamp.studyflow_api.repository.LifeAreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LifeAreaService {

    @Autowired
    private LifeAreaRepository LifeAreaRepository;

    public LifeAreaResponseDTO add(Integer userId, LifeAreaResponseDTO dto) {
        LifeArea lifeArea = new LifeArea();
        applyFields(lifeArea, dto);
        // Ownership is ALWAYS derived server-side; ignore whatever the client sent.
        lifeArea.setUserId(userId);
        return LifeAreaResponseDTO.convertToDTO(LifeAreaRepository.save(lifeArea));
    }

    public LifeAreaResponseDTO modify(Integer userId, Integer id, LifeAreaResponseDTO dto) {
        LifeArea lifeArea = findOwned(userId, id);
        applyFields(lifeArea, dto);
        return LifeAreaResponseDTO.convertToDTO(LifeAreaRepository.save(lifeArea));
    }

    private void applyFields(LifeArea lifeArea, LifeAreaResponseDTO dto) {
        lifeArea.setName(dto.getName());
        lifeArea.setColor(dto.getColor());
        lifeArea.setActive(dto.getActive());
        lifeArea.setAcademicYear(dto.getAcademicYear());
    }

    public List<LifeAreaResponseDTO> findAll(Integer userId) {
        return LifeAreaRepository.findByUserId(userId).stream()
                .map(LifeAreaResponseDTO::convertToDTO)
                .collect(Collectors.toList());
    }

    public void delete(Integer userId, Integer id) {
        LifeAreaRepository.delete(findOwned(userId, id));
    }

    public LifeAreaResponseDTO findById(Integer userId, Integer id) {
        return LifeAreaResponseDTO.convertToDTO(findOwned(userId, id));
    }

    public List<LifeAreaResponseDTO> findByName(Integer userId, String name) {
        return LifeAreaRepository.findByUserId(userId).stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .map(LifeAreaResponseDTO::convertToDTO)
                .collect(Collectors.toList());
    }

    /** Cross-account access is indistinguishable from "does not exist". */
    private LifeArea findOwned(Integer userId, Integer id) {
        return LifeAreaRepository.findById(id)
                .filter(a -> a.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LifeArea not found"));
    }
}
