package com.iescamp.studyflow.utils;

import java.io.InputStream;
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

    public static String getApiUrl() {
        // Valor por defecto si falla la carga
        return properties.getProperty("api.url", "http://localhost:8888/api");
    }
}