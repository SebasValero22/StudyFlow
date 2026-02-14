package com.iescamp.studyflow.utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

public class JsonMapper {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // Configuración para que no falle si la API envía campos de más
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper get() {
        return mapper;
    }
}