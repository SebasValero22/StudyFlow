package com.iescamp.studyflow_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Filesystem-backed storage (default provider). Zero-cost on a single node;
 * swap to GoogleDriveStorageService for multi-device BYO-cloud setups.
 */
@Component
@ConditionalOnProperty(name = "studyflow.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path baseDir;

    public LocalStorageService(@Value("${studyflow.storage.local-dir:./data/files}") String dir) {
        this.baseDir = Path.of(dir).toAbsolutePath().normalize();
    }

    @Override
    public String store(Integer userId, String originalFilename, byte[] data) {
        try {
            String safeName = (originalFilename == null || originalFilename.isBlank()
                    ? "file" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_"));
            Path userDir = baseDir.resolve(String.valueOf(userId));
            Files.createDirectories(userDir);
            Path target = userDir.resolve(UUID.randomUUID() + "-" + safeName);
            Files.write(target, data);
            return target.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Storage write failed", e);
        }
    }

    @Override
    public byte[] retrieve(String path) {
        try {
            Path p = Path.of(path).toAbsolutePath().normalize();
            // Containment check: never serve anything outside the storage root
            if (!p.startsWith(baseDir)) return null;
            return Files.exists(p) ? Files.readAllBytes(p) : null;
        } catch (IOException e) {
            return null;
        }
    }
}
