package com.iescamp.studyflow_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class NyraAutoScheduleResponseDTO {
    private List<NyraTaskUpdateDTO> tasks;
}
