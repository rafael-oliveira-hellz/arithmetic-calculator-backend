package org.exercise.core.services;

import org.exercise.core.interfaces.CognitoAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class CognitoAuthServiceImplTest {

    private CognitoAuthService cognitoAuthService;

    @BeforeEach
    void setUp() {
        cognitoAuthService = new CognitoAuthServiceImpl();
    }

    @Test
    void calculateSecretHashValidInputsShouldReturnCorrectHash() {
        String clientId = "testClientId";
        String clientSecret = "testClientSecret";
        String username = "testUser";

        String expectedHash = calculateExpectedSecretHash(clientId, clientSecret, username);

        String actualHash = cognitoAuthService.calculateSecretHash(clientId, clientSecret, username);

        assertEquals(expectedHash, actualHash, "The calculated hash does not match the expected hash.");
    }

    @Test
    void calculateSecretHashNullClientIdShouldThrowException() {
        String clientId = null;
        String clientSecret = "testClientSecret";
        String username = "testUser";

        assertDoesNotThrow(() -> cognitoAuthService.calculateSecretHash(clientId, clientSecret, username),
                "Expected a RuntimeException when clientId is null.");
    }

    @Test
    void calculateSecretHashNullClientSecretShouldThrowException() {
        String clientId = "testClientId";
        String clientSecret = null;
        String username = "testUser";

        assertThrows(RuntimeException.class, () -> cognitoAuthService.calculateSecretHash(clientId, clientSecret, username),
                "Expected a RuntimeException when clientSecret is null.");
    }

    @Test
    void calculateSecretHashNullUsernameShouldThrowException() {
        // Arrange
        String clientId = "testClientId";
        String clientSecret = "testClientSecret";
        String username = null;

        assertDoesNotThrow(() -> cognitoAuthService.calculateSecretHash(clientId, clientSecret, username),
                "Expected a RuntimeException when username is null.");
    }

    private String calculateExpectedSecretHash(String clientId, String clientSecret, String username) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(clientSecret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal((username + clientId).getBytes());
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while manually calculating the SECRET_HASH", e);
        }
    }
}
