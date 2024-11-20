package org.exercise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    private final String backendUrl = System.getenv("BACKEND_URL");
    private final String frontendUrl = System.getenv("FRONTEND_URL");

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(backendUrl, frontendUrl, "http://localhost:3000") 
                        .allowedMethods("POST","OPTIONS")
                        .allowedHeaders("Content-Type", "Authorization", "accessToken") 
                        .exposedHeaders("Authorization", "accessToken") 
                        .allowCredentials(true); 
            }
        };
    }
}
