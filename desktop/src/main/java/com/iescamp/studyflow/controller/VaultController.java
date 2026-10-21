package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.VaultItem;
import com.iescamp.studyflow.service.VaultService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.util.List;

public class VaultController {

    @FXML private VBox vaultRoot;
    @FXML private ListView<VaultItem> vaultListView;
    @FXML private TextField quickVaultInput;
    @FXML private Label vaultStatusLabel;

    private final VaultService vaultService = new VaultService();

    @FXML
    public void initialize() {
        loadVaultItems();
    }

    private void loadVaultItems() {
        try {
            List<VaultItem> items = vaultService.getAllVaultItems();
            vaultListView.getItems().setAll(items);
            vaultStatusLabel.setText(items.size() + " items in Vault");
        } catch (Exception e) {
            e.printStackTrace();
            vaultStatusLabel.setText("Error loading vault");
        }
    }

    @FXML
    private void addQuickVaultItem() {
        String title = quickVaultInput.getText().trim();
        if (title.isEmpty()) return;

        VaultItem item = new VaultItem();
        item.setTitle(title);
        item.setDomain("work");
        item.setReason("Added via quick capture");

        try {
            vaultService.saveVaultItem(item);
            quickVaultInput.clear();
            loadVaultItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteSelectedVaultItem() {
        VaultItem selected = vaultListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            vaultService.deleteVaultItem(selected.getVaultItemId());
            loadVaultItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}