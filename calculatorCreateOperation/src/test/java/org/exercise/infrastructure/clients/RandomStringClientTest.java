package org.exercise.infrastructure.clients;

import org.exercise.core.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RandomStringClientTest {

    @InjectMocks
    private RandomStringClient randomStringClient;

    @BeforeAll
    static void setUpEnvironment() {
        System.setProperty("RSCLIENT_BASE_URL", "http://mock-api.com"); // Valid URL
        System.setProperty("RSCLIENT_LEN", "8");
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void fetchRandomString_negativeNumber_throwsBadRequestException() {
        assertThrows(BadRequestException.class, () -> randomStringClient.fetchRandomString(-1));
    }

    @Test
    void fetchRandomString_exceedsLimit_throwsBadRequestException() {
        assertThrows(BadRequestException.class, () -> randomStringClient.fetchRandomString(10001));
    }

}
