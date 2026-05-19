package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.File;
import com.iescamp.studyflow_api.model.enums.FileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Integer> {
    // Buscar por nombre de archivo
    List<File> findByFileName(String fileName);

    // Buscar por tipo/extension
    List<File> findByFileType(FileType fileType);
}
