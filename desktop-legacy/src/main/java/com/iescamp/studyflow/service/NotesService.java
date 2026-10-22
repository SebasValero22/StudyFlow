package com.iescamp.studyflow.service;

import com.iescamp.studyflow.utils.ConfigLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * File-backed Markdown vault, Obsidian-style: one folder of .md files.
 * Location defaults to ~/StudyFlow/vault and is overrideable in Settings.
 */
public class NotesService {

    public record NoteInfo(String name, long lastModified) {}

    public Path vaultDir() throws IOException {
        Path dir = ConfigLoader.getVaultDir();
        Files.createDirectories(dir);
        return dir;
    }

    public List<NoteInfo> list() throws IOException {
        Path dir = vaultDir();
        List<NoteInfo> notes = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.md")) {
            for (Path p : stream) {
                FileTime time = Files.getLastModifiedTime(p);
                notes.add(new NoteInfo(p.getFileName().toString(), time.toMillis()));
            }
        }
        notes.sort(Comparator.comparingLong(NoteInfo::lastModified).reversed());
        return notes;
    }

    public String read(String name) throws IOException {
        return Files.readString(resolve(name), StandardCharsets.UTF_8);
    }

    public void write(String name, String content) throws IOException {
        String safeContent = content == null ? "" : content;
        
        // Parse and update YAML frontmatter
        String yaml = "";
        String body = safeContent;
        java.util.Map<String, String> props = new java.util.LinkedHashMap<>();
        
        if (safeContent.startsWith("---\n") || safeContent.startsWith("---\r\n")) {
            int endIdx = safeContent.indexOf("\n---", 3);
            if (endIdx != -1) {
                yaml = safeContent.substring(safeContent.indexOf('\n') + 1, endIdx);
                int nextLine = safeContent.indexOf('\n', endIdx + 4);
                body = nextLine != -1 ? safeContent.substring(nextLine + 1) : "";
                
                String[] lines = yaml.split("\n");
                for (String line : lines) {
                    if (line.contains(":")) {
                        String[] parts = line.split(":", 2);
                        if (parts.length == 2) {
                            props.put(parts[0].trim(), parts[1].trim());
                        }
                    }
                }
            }
        }
        
        // Stamp properties
        String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (!props.containsKey("created")) {
            props.put("created", now);
        }
        props.put("modified", now);
        props.put("device", System.getProperty("user.name") + "-pc");
        
        StringBuilder newContent = new StringBuilder();
        newContent.append("---\n");
        for (java.util.Map.Entry<String, String> e : props.entrySet()) {
            newContent.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        newContent.append("---\n").append(body);
        
        Files.writeString(resolve(name), newContent.toString(), StandardCharsets.UTF_8);
    }

    public void delete(String name) throws IOException {
        Files.deleteIfExists(resolve(name));
    }

    public void rename(String oldName, String newName) throws IOException {
        Path target = resolve(newName);
        if (Files.exists(target)) {
            throw new IOException("A note with that name already exists");
        }
        Files.move(resolve(oldName), target);
    }

    public boolean exists(String name) {
        try {
            return Files.exists(resolve(name));
        } catch (IOException e) {
            return false;
        }
    }

    /** Sanitizes a note title into a safe .md filename. Rejects path traversal. */
    public String sanitize(String raw) throws IOException {
        String name = raw == null ? "" : raw.trim();
        name = name.replaceAll("[\\\\/:*?\"<>|]", "").trim();
        if (name.isEmpty()) {
            throw new IOException("Note name cannot be empty");
        }
        if (!name.toLowerCase().endsWith(".md")) {
            name += ".md";
        }
        if (name.contains("..")) {
            throw new IOException("Invalid note name");
        }
        return name;
    }

    private Path resolve(String name) throws IOException {
        String safe = sanitize(name);
        return vaultDir().resolve(safe).normalize();
    }
}
