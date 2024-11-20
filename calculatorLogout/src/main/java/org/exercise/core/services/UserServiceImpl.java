package org.exercise.core.services;

import lombok.RequiredArgsConstructor;
import org.exercise.config.CognitoConfig;
import org.exercise.core.exceptions.LogoutException;
import org.exercise.core.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GlobalSignOutRequest;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final CognitoConfig cognitoConfig;
    private final Supplier<CognitoIdentityProviderClient> cognitoClientSupplier = this::createCognitoClient;

    @Override
    public void logoutUser(String accessToken) {
        logger.info("Starting logout process for user.");

        try {
            GlobalSignOutRequest signOutRequest = GlobalSignOutRequest.builder()
                    .accessToken(accessToken)
                    .build();

            logger.debug("Sending GlobalSignOutRequest to Cognito.");
            cognitoClientSupplier.get().globalSignOut(signOutRequest);
            logger.info("Logout successful for the user with access token: {}", maskToken(accessToken));
        } catch (CognitoIdentityProviderException e) {
            logger.error("CognitoIdentityProviderException occurred during logout: {}. AWS error details: {}",
                    e.getMessage(), e.awsErrorDetails().errorMessage());
            throw new LogoutException("Logout failed: " + e.awsErrorDetails().errorMessage());
        } catch (IllegalStateException e) {
            logger.error("IllegalStateException detected: connection pool might have been shut down. Reinitializing client...");
            throw new LogoutException("Cognito client is unavailable. Please try again later.");
        } catch (Exception e) {
            logger.error("Unexpected error during logout: {}", e.getMessage(), e);
            throw new LogoutException("Unexpected error occurred during logout.");
        }
    }

    private CognitoIdentityProviderClient createCognitoClient() {
        logger.info("Initializing Cognito client.");
        return cognitoConfig.cognitoClient();
    }

    String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "N/A";
        }
        return token.substring(0, 5) + "******" + token.substring(token.length() - 5);
    }
}