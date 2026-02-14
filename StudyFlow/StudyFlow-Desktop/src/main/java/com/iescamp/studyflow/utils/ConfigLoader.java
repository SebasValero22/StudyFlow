package com.iescamp.studyflow.utils;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) throw new RuntimeException("Archivo config.properties no encontrado");
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getApiUrl() {
        return properties.getProperty("api.url", "http://localhost:8888/api");
    }
}