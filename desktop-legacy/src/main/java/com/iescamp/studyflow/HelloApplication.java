package com.iescamp.studyflow;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.iescamp.studyflow.database.DatabaseHelper;
import com.iescamp.studyflow.model.User;
import com.iescamp.studyflow.service.SyncService;
import com.iescamp.studyflow.utils.UserSession;
import com.iescamp.studyflow.utils.ViewSwitcher;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class HelloApplication extends Application implements NativeKeyListener {

    private QuickCaptureView quickCaptureView;
    private TrayIcon trayIcon;

    @Override
    public void start(Stage stage) {
        // Prevent JVM from shutting down when the window is closed/hidden
        Platform.setImplicitExit(false);

        ViewSwitcher.setStage(stage);
        stage.setTitle("StudyFlow - Life OS");

        // Set application icon for stage
        loadStageIcon(stage);

        DatabaseHelper.initializeSchema();

        // System Tray
        setupSystemTray(stage);

        // Window close handler -> minimize/hide to tray instead of exiting
        stage.setOnCloseRequest(event -> {
            event.consume(); // Cancel standard exit
            stage.hide();
        });

        // Auto-login persistence
        boolean restored = UserSession.getInstance().restoreSession();
        if (restored && UserSession.getInstance().getUser() != null) {
            User user = UserSession.getInstance().getUser();
            System.out.println("Auto-login restored for user: " + user.getEmail() + " (ID: " + user.getUserId() + ")");
            SyncService.getInstance().onUserLoggedIn(user);
            SyncService.getInstance().startBackgroundSync();
            ViewSwitcher.loadView("main_dashboard.fxml");
        } else {
            ViewSwitcher.loadView("login_view.fxml");
        }

        stage.show();

        // Setup Quick Capture
        quickCaptureView = new QuickCaptureView();
        setupGlobalShortcut();
    }

    private void loadStageIcon(Stage stage) {
        try (InputStream is = getClass().getResourceAsStream("/com/iescamp/studyflow/icons/icon.png")) {
            if (is != null) {
                stage.getIcons().add(new Image(is));
            }
        } catch (Exception e) {
            System.err.println("Could not load stage icon: " + e.getMessage());
        }
    }

    private void setupSystemTray(Stage stage) {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported on this platform.");
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            java.awt.Image image = loadTrayImage();

            PopupMenu popup = new PopupMenu();

            MenuItem openItem = new MenuItem("Open StudyFlow");
            openItem.addActionListener(e -> Platform.runLater(() -> {
                stage.show();
                stage.toFront();
                stage.setIconified(false);
            }));

            MenuItem quickCaptureItem = new MenuItem("Quick Capture (Ctrl + Space)");
            quickCaptureItem.addActionListener(e -> Platform.runLater(() -> {
                if (quickCaptureView != null) {
                    quickCaptureView.show();
                }
            }));

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> Platform.runLater(() -> exitApplication(stage)));

            popup.add(openItem);
            popup.add(quickCaptureItem);
            popup.addSeparator();
            popup.add(exitItem);

            trayIcon = new TrayIcon(image, "StudyFlow", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> Platform.runLater(() -> {
                stage.show();
                stage.toFront();
                stage.setIconified(false);
            }));

            tray.add(trayIcon);
        } catch (Exception e) {
            System.err.println("Failed to initialize system tray: " + e.getMessage());
        }
    }

    private java.awt.Image loadTrayImage() {
        try (InputStream is = getClass().getResourceAsStream("/com/iescamp/studyflow/icons/icon.png")) {
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (Exception e) {
            System.err.println("Could not load tray icon from resources: " + e.getMessage());
        }
        // Fallback programmatic 16x16 icon
        BufferedImage fallback = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fallback.createGraphics();
        g.setColor(new java.awt.Color(99, 102, 241));
        g.fillRoundRect(0, 0, 16, 16, 4, 4);
        g.setColor(java.awt.Color.WHITE);
        g.drawString("S", 4, 13);
        g.dispose();
        return fallback;
    }

    private void setupGlobalShortcut() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook: " + ex.getMessage());
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // Ctrl + Space
        if (e.getKeyCode() == NativeKeyEvent.VC_SPACE && (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) {
            Platform.runLater(() -> {
                if (quickCaptureView != null) {
                    quickCaptureView.show();
                }
            });
        }
    }

    private void exitApplication(Stage stage) {
        if (trayIcon != null && SystemTray.isSupported()) {
            try {
                SystemTray.getSystemTray().remove(trayIcon);
            } catch (Exception ignored) {}
        }
        stop();
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void stop() {
        SyncService.getInstance().stopBackgroundSync();
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
