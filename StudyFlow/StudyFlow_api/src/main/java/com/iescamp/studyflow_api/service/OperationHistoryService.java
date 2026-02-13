package com.iescamp.studyflow_api.service;

import com.iescamp.studyflow_api.dto.OperationHistoryDTO;
import com.iescamp.studyflow_api.model.OperationHistory;
import com.iescamp.studyflow_api.repository.OperationHistoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationHistoryService {

    @Autowired
    private OperationHistoryRepository operationHistoryRepository;

    // 1. Changed to PUBLIC so controller can see it
    public OperationHistoryDTO add(OperationHistoryDTO dto) {
        OperationHistory oH = new OperationHistory();
        oH.setUser(dto.getUser());
        oH.setDescription(dto.getDescription());
        oH.setActionType(dto.getActionType()); // Fixed: was setting description twice
        oH.setOperationDate(dto.getOperationDate());

        OperationHistory savedOperation = operationHistoryRepository.save(oH);
        return OperationHistoryDTO.convertToDTO(savedOperation);
    }

    // 2. FIXED: Proper update logic (Find then Update)
    public OperationHistoryDTO update(Integer id, OperationHistoryDTO dto) {
        OperationHistory oH = operationHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Operation not found"));

        oH.setDescription(dto.getDescription());
        oH.setActionType(dto.getActionType());
        oH.setOperationDate(dto.getOperationDate());

        OperationHistory updated = operationHistoryRepository.save(oH);
        return OperationHistoryDTO.convertToDTO(updated);
    }

    // 3. REMOVED: Controller annotations (@DeleteMapping) belong in the Controller
    @Transactional
    public void delete(Integer historyId) {
        if (!operationHistoryRepository.existsById(historyId)) {
            throw new RuntimeException("Operation ID not found");
        }
        operationHistoryRepository.deleteById(historyId);
    }

    public OperationHistoryDTO findById(Integer historyId) {
        OperationHistory oH = operationHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("Operation ID not found"));
        // Fixed: Pass the object 'oH', not the repository
        return OperationHistoryDTO.convertToDTO(oH);
    }

    public List<OperationHistoryDTO> findAll() {
        return operationHistoryRepository.findAll().stream()
                .map(OperationHistoryDTO::convertToDTO)
                .collect(Collectors.toList());
    }
    public List<OperationHistoryDTO> getHistoryByUser(Integer userId) {
        return operationHistoryRepository.findByUser_UserId(userId).stream() // Cambiado aquí
                .map(OperationHistoryDTO::convertToDTO)
                .toList();
    }
}