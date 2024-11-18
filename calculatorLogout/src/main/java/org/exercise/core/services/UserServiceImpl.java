package org.exercise.core.services;

import lombok.RequiredArgsConstructor;
import org.exercise.core.config.CognitoConfig;
import org.exercise.core.exceptions.LogoutException;
import org.exercise.core.interfaces.UserService;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GlobalSignOutRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GlobalSignOutResponse;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final CognitoConfig cognitoConfig;

    public void logoutUser(String accessToken) {
        try {
            GlobalSignOutRequest signOutRequest = GlobalSignOutRequest.builder()
                    .accessToken(accessToken)
                    .build();
            try (CognitoIdentityProviderClient cognitoIdentityProviderClient = cognitoConfig.cognitoClient()) {
                cognitoIdentityProviderClient.globalSignOut(signOutRequest);
            }
        } catch (CognitoIdentityProviderException e) {
            throw new LogoutException("Logout failed: " + e.awsErrorDetails().errorMessage());
        }
    }
}
