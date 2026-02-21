package com.iescamp.studyflow.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Importante
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonMapper {
    private static ObjectMapper mapper;

    public static ObjectMapper get() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
        return mapper;
    }
}