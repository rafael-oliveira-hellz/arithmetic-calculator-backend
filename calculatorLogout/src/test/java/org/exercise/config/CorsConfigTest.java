package org.exercise.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.function.serverless.web.ServerlessAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestConfiguration
@ContextConfiguration(classes = CorsConfig.class)
@EnableAutoConfiguration(exclude = {ServerlessAutoConfiguration.class})
class CorsConfigTest {

    @Test
    void testCorsConfigurer() {
        CorsConfig corsConfig = new CorsConfig();
        WebMvcConfigurer webMvcConfigurer = corsConfig.corsConfigurer();

        assertNotNull(webMvcConfigurer);
    }
}
