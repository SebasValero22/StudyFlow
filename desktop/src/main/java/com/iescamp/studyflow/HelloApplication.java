package com.iescamp.studyflow;

import com.iescamp.studyflow.utils.ViewSwitcher;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;

public class HelloApplication extends Application implements NativeKeyListener {

    private QuickCaptureView quickCaptureView;

    @Override
    public void start(Stage stage) throws IOException {

        ViewSwitcher.setStage(stage);
        stage.setTitle("StudyFlow - Life OS");
        com.iescamp.studyflow.database.DatabaseHelper.initializeSchema();
        // Sync starts after login (LoginController) — no token yet at boot.
        ViewSwitcher.loadView("login_view.fxml");

        stage.show();

        // Setup Quick Capture
        quickCaptureView = new QuickCaptureView();
        setupGlobalShortcut();
    }

    private void setupGlobalShortcut() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // Ctrl + Space
        if (e.getKeyCode() == NativeKeyEvent.VC_SPACE && (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) {
            quickCaptureView.show();
        }
    }

    @Override
    public void stop() {
        com.iescamp.studyflow.service.SyncService.getInstance().stopBackgroundSync();
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
