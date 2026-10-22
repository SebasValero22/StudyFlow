package com.iescamp.studyflow.utils;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Applies the persisted light/dark theme to a scene.
 */
public final class ThemeManager {

    private ThemeManager() {}

    public static void apply(Stage stage) {
        if (stage == null) {
            return;
        }
        apply(stage.getScene());
    }

    public static void apply(Scene scene) {
        if (scene == null) {
            return;
        }
        scene.getStylesheets().removeIf(s -> s.contains("/css/theme-"));
        String theme = AppSettings.get().getTheme();
        if (!"dark".equalsIgnoreCase(theme)) {
            theme = "light";
        }
        URL css = ThemeManager.class.getResource(
                "/com/iescamp/studyflow/css/theme-" + theme.toLowerCase() + ".css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.err.println("Theme CSS not found for: " + theme);
        }
    }
}
