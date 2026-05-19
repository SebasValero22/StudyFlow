package com.iescamp.studyflow_api.service;


import com.iescamp.studyflow_api.dto.FileDTO;
import com.iescamp.studyflow_api.model.File;
import com.iescamp.studyflow_api.model.enums.FileType;
import com.iescamp.studyflow_api.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {
    @Autowired
    private FileRepository fileRepository;

    public FileDTO add(FileDTO dto){
        File file = new File();
        file.setFileName(dto.getFileName());
        return FileDTO.convertToDTO(fileRepository.save(file));
    }

    public void delete(Integer id){
        if (!fileRepository.existsById(id))throw new RuntimeException ("no existe id");
        fileRepository.deleteById(id);
        System.out.println("file deleted succesfully");
    }


    public List<FileDTO> findAll(){
        return fileRepository.findAll().stream().map(FileDTO::convertToDTO).toList();
    }
    public FileDTO findById(Integer id){
        return FileDTO.convertToDTO(fileRepository.findById(id).orElseThrow());
    }
    public FileDTO modify(Integer id, FileDTO dto){
        File file = fileRepository.findById(id).orElseThrow();
        file.setFileName(dto.getFileName());
        return FileDTO.convertToDTO(fileRepository.save(file));
    }

    // 1. Recibe un String del Controlador (ej: "pdf")
    public List<FileDTO> findByType(String type){

        FileType enumType;
        try {
            // 2. Convierte el String "pdf" al Enum FileType.PDF
            enumType = FileType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de archivo no válido: " + type);
        }

        // 3. Llama al Repositorio usando el nombre correcto: findByFileType
        List<File> files = fileRepository.findByFileType(enumType);

        if (files.isEmpty()){
            throw new RuntimeException("No files found of type: " + type);
        }

        return files.stream()
                .map(FileDTO::convertToDTO)
                .toList();
    }

}
