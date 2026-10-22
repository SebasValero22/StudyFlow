package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.service.NotesService;
import com.iescamp.studyflow.utils.AppSettings;
import com.iescamp.studyflow.utils.Markdown;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Obsidian-style notes vault: file list, editor, live Markdown preview
 * with clickable [[WikiLinks]] (red when the target does not exist yet).
 */
public class NotesController {

    @FXML private ListView<NotesService.NoteInfo> notesList;
    @FXML private TextArea editor;
    @FXML private WebView preview;
    @FXML private Label statusLabel;
    @FXML private SplitPane splitPane;
    @FXML private javafx.scene.layout.FlowPane propertiesChipRow;

    private final NotesService notesService = new NotesService();
    private String currentNote;
    private String savedSnapshot = "";
    private boolean dirty;
    private String currentPage = "";
    private final PauseTransition previewDebounce = new PauseTransition(Duration.millis(400));

    @FXML
    public void initialize() {
        notesList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NotesService.NoteInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    LocalDateTime dt = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(item.lastModified()), ZoneId.systemDefault());
                    setText(displayName(item.name()) + "\n"
                            + dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
        });
        notesList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.name().equals(currentNote)) {
                openNote(newV.name(), false);
            }
        });

        editor.textProperty().addListener((obs, oldV, newV) -> {
            if (currentNote != null) {
                setDirty(!newV.equals(savedSnapshot));
            }
            previewDebounce.setOnFinished(e -> renderPreview());
            previewDebounce.playFromStart();
        });

        preview.getEngine().locationProperty().addListener((obs, oldLoc, newLoc) -> {
            if (newLoc == null || newLoc.equals("about:blank")) {
                return;
            }
            if (newLoc.startsWith("note:")) {
                String target = URLDecoder.decode(newLoc.substring("note:".length()), StandardCharsets.UTF_8);
                openNote(target, true);
            } else if (newLoc.startsWith("http://") || newLoc.startsWith("https://")) {
                try {
                    Desktop.getDesktop().browse(new URI(newLoc));
                } catch (Exception ex) {
                    status("Could not open link: " + ex.getMessage());
                }
                reloadPreview();
            }
        });

        refreshList(null);
    }

    @FXML
    private void handleNewNote() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New note");
        dialog.setHeaderText(null);
        dialog.setContentText("Note name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            try {
                String file = notesService.sanitize(name);
                if (notesService.exists(file)) {
                    status("Note already exists: " + displayName(file));
                    openNote(file, false);
                    return;
                }
                notesService.write(file, "# " + displayName(file).replace(".md", "") + "\n\n");
                refreshList(file);
                status("Created " + displayName(file));
            } catch (Exception e) {
                status("Could not create note: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleSave() {
        if (currentNote == null) {
            return;
        }
        try {
            notesService.write(currentNote, editor.getText());
            // Read back to show the stamped frontmatter
            String updatedContent = notesService.read(currentNote);
            int cursor = editor.getCaretPosition();
            editor.setText(updatedContent);
            editor.positionCaret(cursor);
            
            savedSnapshot = editor.getText();
            setDirty(false);
            renderPreview();
            refreshList(currentNote);
            status("Saved " + displayName(currentNote));
        } catch (Exception e) {
            status("Save failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (currentNote == null) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete '" + displayName(currentNote) + "' permanently?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }
        try {
            notesService.delete(currentNote);
            currentNote = null;
            editor.clear();
            setDirty(false);
            preview.getEngine().loadContent("");
            refreshList(null);
            status("Deleted.");
        } catch (Exception e) {
            status("Delete failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleRename() {
        if (currentNote == null) {
            return;
        }
        TextInputDialog dialog = new TextInputDialog(displayName(currentNote).replace(".md", ""));
        dialog.setTitle("Rename note");
        dialog.setHeaderText(null);
        dialog.setContentText("New name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            try {
                String target = notesService.sanitize(name);
                notesService.rename(currentNote, target);
                currentNote = target;
                refreshList(target);
                status("Renamed to " + displayName(target));
            } catch (Exception e) {
                status("Rename failed: " + e.getMessage());
            }
        });
    }

    private void openNote(String name, boolean createIfMissing) {
        try {
            String file = notesService.sanitize(name);
            if (!notesService.exists(file)) {
                if (!createIfMissing) {
                    return;
                }
                notesService.write(file, "# " + displayName(file).replace(".md", "") + "\n\n");
                refreshList(file);
            }
            currentNote = file;
            editor.setText(notesService.read(file));
            savedSnapshot = editor.getText();
            setDirty(false);
            renderPreview();
            selectInList(file);
        } catch (Exception e) {
            status("Could not open note: " + e.getMessage());
        }
    }

    private void renderPreview() {
        boolean dark = "dark".equalsIgnoreCase(AppSettings.get().getTheme());
        String text = editor.getText();
        updatePropertiesChips(text);
        String body = Markdown.toHtml(text, this::linkTargetExists);
        currentPage = Markdown.toPage(body, dark);
        preview.getEngine().loadContent(currentPage);
    }

    private void updatePropertiesChips(String text) {
        if (propertiesChipRow == null) return;
        propertiesChipRow.getChildren().clear();
        if (text == null || text.isEmpty()) return;
        if (text.startsWith("---\n") || text.startsWith("---\r\n")) {
            int endIdx = text.indexOf("\n---", 3);
            if (endIdx != -1) {
                String yaml = text.substring(text.indexOf('\n') + 1, endIdx);
                String[] lines = yaml.split("\n");
                for (String line : lines) {
                    if (line.contains(":")) {
                        String[] parts = line.split(":", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String val = parts[1].trim();
                            Label chip = new Label(key + ": " + val);
                            chip.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-padding: 3 8 3 8; -fx-background-radius: 12; -fx-font-size: 11px;");
                            propertiesChipRow.getChildren().add(chip);
                        }
                    }
                }
            }
        }
    }

    private void reloadPreview() {
        String page = currentPage;
        Platform.runLater(() -> preview.getEngine().loadContent(page));
    }

    private boolean linkTargetExists(String target) {
        String t = target.trim();
        return notesService.exists(t) || notesService.exists(t + ".md");
    }

    private void refreshList(String select) {
        try {
            List<NotesService.NoteInfo> notes = notesService.list();
            notesList.setItems(FXCollections.observableArrayList(notes));
            if (select != null) {
                selectInList(select);
            } else if (!notes.isEmpty() && currentNote == null) {
                notesList.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            status("Could not list vault: " + e.getMessage());
        }
    }

    private void selectInList(String file) {
        for (NotesService.NoteInfo info : notesList.getItems()) {
            if (info.name().equals(file)) {
                notesList.getSelectionModel().select(info);
                notesList.scrollTo(info);
                return;
            }
        }
    }

    private void setDirty(boolean value) {
        dirty = value;
        status(dirty ? "● Unsaved changes" : "");
    }

    private void status(String message) {
        statusLabel.setText(message == null ? "" : message);
    }

    private static String displayName(String file) {
        return file.endsWith(".md") ? file.substring(0, file.length() - 3) : file;
    }
}
