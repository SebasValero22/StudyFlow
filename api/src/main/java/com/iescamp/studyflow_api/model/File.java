package com.iescamp.studyflow_api.model;


import com.iescamp.studyflow_api.model.enums.FileType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fileID;

    @ManyToOne
    @JoinColumn(name = "subjectId", nullable = false)
    private Subject subjectId;

    private String fileName;
    private String filePath;
    private String sizeBytes;
    private FileType fileType;



}
