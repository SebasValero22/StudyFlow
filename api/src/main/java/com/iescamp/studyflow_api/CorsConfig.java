package com.iescamp.studyflow_api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Aplica a todos los endpoints de tu API
                        .allowedOriginPatterns("*") // Permite peticiones de Angular (localhost:4200)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // Abre todos los métodos
                        .allowedHeaders("*") // Permite todos los headers
                        .allowCredentials(false);
            }
        };
    }
}