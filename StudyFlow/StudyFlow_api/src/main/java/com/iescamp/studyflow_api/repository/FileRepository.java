package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.File;
import com.iescamp.studyflow_api.model.enums.FileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Integer> {
    // Search by the file name
    List<File> findByFileName(String fileName);
    // If you want to find files by their extension/type
    List<File> findByFileType(FileType fileType);}
