package com.iescamp.studyflow_api.dto;


import com.iescamp.studyflow_api.model.OperationHistory;
import com.iescamp.studyflow_api.model.User;
import lombok.Data;

import java.util.Date;

@Data
public class OperationHistoryDTO {
        private Integer historyId;
        private String description;
        private String actionType;
        private Date operationDate;
        private User user;

        public static OperationHistoryDTO convertToDTO(OperationHistory entity) {
            OperationHistoryDTO dto = new OperationHistoryDTO();
            dto.setHistoryId(entity.getHistoryId());
            dto.setDescription(entity.getDescription());
            dto.setActionType(entity.getActionType());
            dto.setOperationDate(entity.getOperationDate());
            dto.setUser(entity.getUser());
            return dto;
        }
}
