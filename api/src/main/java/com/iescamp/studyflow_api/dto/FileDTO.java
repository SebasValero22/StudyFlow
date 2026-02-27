package com.iescamp.studyflow_api.dto;

import com.iescamp.studyflow_api.model.File;
import com.iescamp.studyflow_api.model.Subject;
import com.iescamp.studyflow_api.model.enums.FileType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class FileDTO {

    private Integer fileID;
    private Subject subjectId;
    private String fileName;
    private String filePath;
    private String sizeBytes;
    private FileType fileType;

    public static FileDTO convertToDTO(File file){
        FileDTO dto = new FileDTO();
        dto.setFileID(file.getFileID());
        dto.setFileName(file.getFileName());
        dto.setFilePath(file.getFilePath());
        dto.setFileType(file.getFileType());
        dto.setSizeBytes(file.getSizeBytes());
        dto.setSubjectId(file.getSubjectId());
        return dto;
    }
}
