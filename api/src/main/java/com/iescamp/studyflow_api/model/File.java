package com.iescamp.studyflow_api.model;


import com.iescamp.studyflow_api.model.enums.FileType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileId")
    private Integer fileID;

    @ManyToOne
    @JoinColumn(name = "lifeAreaId", nullable = false)
    private LifeArea LifeAreaId;

    @Column(name = "filename", nullable = false)
    private String fileName;

    @Column(name = "filepath", nullable = false)
    private String filePath;

    @Column(name = "sizeBytes")
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "mimeType")
    private FileType fileType;

}
