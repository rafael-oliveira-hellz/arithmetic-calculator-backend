package org.exercise.core.interfaces;

public interface CognitoAuthService {
    String calculateSecretHash(String clientId, String clientSecret, String username);
}
