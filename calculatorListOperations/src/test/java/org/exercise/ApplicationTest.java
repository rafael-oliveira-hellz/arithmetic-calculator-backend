package org.exercise;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
class ApplicationTest {

    @Test
    void main() {
        System.setProperty("spring.profiles.active", "test");
        Application.main(new String[] {});
    }
}