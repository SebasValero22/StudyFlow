package com.iescamp.studyflow.model;
import lombok.Data;
@Data
public class Files {

    private int fileId;
    private int subjectId;
    private String fileName;
    private String path;
    private  long fileSize;
    private String fileType;


}
