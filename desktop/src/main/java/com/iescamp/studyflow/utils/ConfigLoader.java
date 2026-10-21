package com.iescamp.studyflow.utils;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        // La barra "/" al principio le dice que busque en la raíz de 'resources'
        try (InputStream input = ConfigLoader.class.getResourceAsStream("/config.properties")) {
            if (input == null) {
                // Si falla, usamos valores por defecto para que no explote
                System.err.println("¡OJO! No encontré config.properties, usando localhost:8888");
            } else {
                properties.load(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * User override (Settings → Cloud) wins; bundled default is the fallback.
     * This is what makes "bring your own cloud" work without rebuilding.
     */
    public static String getApiUrl() {
        String override = AppSettings.get().getApiUrlOverride();
        if (!override.isEmpty()) {
            return override.endsWith("/") ? override.substring(0, override.length() - 1) : override;
        }
        // Valor por defecto si falla la carga
        return properties.getProperty("api.url", "https://studyflow-nuux.onrender.com/api");
    }

    /** Local Obsidian-style vault. Overrideable in Settings → Notes. */
    public static Path getVaultDir() {
        // Tests / CI inject a temp vault so they never touch the real user store.
        String sys = System.getProperty("studyflow.vault");
        if (sys != null && !sys.isBlank()) {
            return Paths.get(sys);
        }
        String override = AppSettings.get().getVaultDirOverride();
        if (!override.isEmpty()) {
            return Paths.get(override);
        }
        return Paths.get(System.getProperty("user.home"), "StudyFlow", "vault");
    }
}
