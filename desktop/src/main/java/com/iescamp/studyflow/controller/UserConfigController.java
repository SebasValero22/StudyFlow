package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.User;
import com.iescamp.studyflow.utils.AppSettings;
import com.iescamp.studyflow.utils.ThemeManager;
import com.iescamp.studyflow.utils.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UserConfigController {
    // Profile
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtNewPassword;
    @FXML private Label lblStatus;

    // Timer
    @FXML private ComboBox<String> cmbPreset;
    @FXML private Spinner<Integer> spnFocus;
    @FXML private Spinner<Integer> spnShort;
    @FXML private Spinner<Integer> spnLong;
    @FXML private Spinner<Integer> spnCycles;
    @FXML private CheckBox chkAutoBreaks;
    @FXML private CheckBox chkAutoFocus;
    @FXML private CheckBox chkSound;
    @FXML private Label lblTimerStatus;

    // Cloud
    @FXML private TextField txtApiUrl;
    @FXML private Label lblCloudStatus;

    // Appearance
    @FXML private ComboBox<String> cmbTheme;

    // Vault
    @FXML private TextField txtVaultDir;
    @FXML private Label lblVaultStatus;

    @FXML
    public void initialize() {
        // Profile
        User currentUser = UserSession.getInstance().getUser();
        if (currentUser != null) {
            txtUsername.setText(currentUser.getName());
            txtEmail.setText(currentUser.getEmail());
        }

        // Timer
        AppSettings s = AppSettings.get();
        cmbPreset.setItems(FXCollections.observableArrayList("Pomodoro", "Animedoro", "Custom"));
        cmbPreset.setValue(toLabel(s.getTimerPreset()));
        cmbPreset.setOnAction(e -> {
            s.applyPreset(toPreset(cmbPreset.getValue()));
            refreshTimerControls();
            lblTimerStatus.setText("Preset applied — remember to Save.");
        });
        spnFocus.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 240, s.getFocusMinutes()));
        spnShort.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, s.getShortBreakMinutes()));
        spnLong.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 180, s.getLongBreakMinutes()));
        spnCycles.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, s.getCyclesBeforeLongBreak()));
        for (Spinner<Integer> sp : new Spinner[]{spnFocus, spnShort, spnLong, spnCycles}) {
            sp.setEditable(true);
        }
        chkAutoBreaks.setSelected(s.isAutoStartBreaks());
        chkAutoFocus.setSelected(s.isAutoStartFocus());
        chkSound.setSelected(s.isSoundEnabled());

        // Cloud
        txtApiUrl.setText(s.getApiUrlOverride());

        // Appearance
        cmbTheme.setItems(FXCollections.observableArrayList("Light", "Dark"));
        cmbTheme.setValue("dark".equalsIgnoreCase(s.getTheme()) ? "Dark" : "Light");
        cmbTheme.setOnAction(e -> {
            s.setTheme("Dark".equals(cmbTheme.getValue()) ? "dark" : "light");
            com.iescamp.studyflow.service.SettingsOutbox.queueCurrentSettings();
            ThemeManager.apply(txtApiUrl.getScene());
        });

        // Vault
        txtVaultDir.setText(s.getVaultDirOverride());
    }

    private void refreshTimerControls() {
        AppSettings s = AppSettings.get();
        setSpinner(spnFocus, s.getFocusMinutes());
        setSpinner(spnShort, s.getShortBreakMinutes());
        setSpinner(spnLong, s.getLongBreakMinutes());
        setSpinner(spnCycles, s.getCyclesBeforeLongBreak());
    }

    private static void setSpinner(Spinner<Integer> spinner, int value) {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory();
        factory.setValue(value);
    }

    private static String toLabel(AppSettings.TimerPreset preset) {
        return switch (preset) {
            case POMODORO -> "Pomodoro";
            case ANIMEDORO -> "Animedoro";
            case CUSTOM -> "Custom";
        };
    }

    private static AppSettings.TimerPreset toPreset(String label) {
        if ("Pomodoro".equals(label)) return AppSettings.TimerPreset.POMODORO;
        if ("Animedoro".equals(label)) return AppSettings.TimerPreset.ANIMEDORO;
        return AppSettings.TimerPreset.CUSTOM;
    }

    @FXML
    private void handleUpdateUser() {
        // Aquí llamarías a tu userService.update(...)
        System.out.println("Updating user...");
        lblStatus.setText("Profile update is not wired to the API yet.");
    }

    @FXML
    private void handleSaveTimer() {
        AppSettings s = AppSettings.get();
        s.applyPreset(toPreset(cmbPreset.getValue()));
        if ("Custom".equals(cmbPreset.getValue())) {
            s.setFocusMinutes(spnFocus.getValue());
            s.setShortBreakMinutes(spnShort.getValue());
            s.setLongBreakMinutes(spnLong.getValue());
            s.setCyclesBeforeLongBreak(spnCycles.getValue());
        }
        s.setAutoStartBreaks(chkAutoBreaks.isSelected());
        s.setAutoStartFocus(chkAutoFocus.isSelected());
        s.setSoundEnabled(chkSound.isSelected());
        com.iescamp.studyflow.service.SettingsOutbox.queueCurrentSettings();
        lblTimerStatus.setText("Saved. Open the Focus Timer to use the new settings.");
    }

    @FXML
    private void handleSaveCloud() {
        AppSettings.get().setApiUrlOverride(txtApiUrl.getText());
        lblCloudStatus.setText("Saved. Takes effect immediately for new requests — re-login to be safe.");
    }

    @FXML
    private void handleTestConnection() {
        String base = txtApiUrl.getText() == null || txtApiUrl.getText().isBlank()
                ? com.iescamp.studyflow.utils.ConfigLoader.getApiUrl()
                : txtApiUrl.getText().trim();
        lblCloudStatus.setText("Pinging " + base + " ...");
        Thread t = new Thread(() -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(8)).build();
                HttpRequest req = HttpRequest.newBuilder(URI.create(base + "/public/ping"))
                        .timeout(Duration.ofSeconds(8)).GET().build();
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                String msg = res.statusCode() == 200
                        ? "Reachable ✓ (" + res.body() + ")"
                        : "HTTP " + res.statusCode();
                Platform.runLater(() -> lblCloudStatus.setText(msg));
            } catch (Exception ex) {
                Platform.runLater(() -> lblCloudStatus.setText("Unreachable: " + ex.getMessage()));
            }
        }, "cloud-ping");
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleSaveVault() {
        AppSettings.get().setVaultDirOverride(txtVaultDir.getText());
        lblVaultStatus.setText("Saved. Takes effect the next time you open Notes.");
    }

    @FXML
    private void handleLogout() {
        UserSession.getInstance().cleanUserSession();
        com.iescamp.studyflow.service.SyncService.getInstance().stopBackgroundSync();
        com.iescamp.studyflow.utils.ViewSwitcher.loadView("login_view.fxml");
    }
}
