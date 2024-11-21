package org.exercise;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@TestConfiguration
class ApplicationTest {

    @Test
    public void main() {
        System.setProperty("spring.profiles.active", "test");
        Application.main(new String[] {});
    }
}