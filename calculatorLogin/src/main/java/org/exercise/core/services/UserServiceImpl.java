package org.exercise.core.services;

import com.auth0.jwt.JWT;
import lombok.extern.slf4j.Slf4j;
import org.exercise.core.dtos.AuthResponse;
import org.exercise.core.entities.User;
import org.exercise.core.exceptions.LoginFailedException;
import org.exercise.core.exceptions.NotFoundException;
import org.exercise.core.interfaces.CognitoAuthService;
import org.exercise.core.interfaces.UserService;
import org.exercise.infrastructure.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.auth0.jwt.interfaces.DecodedJWT;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private static final String CLIENT_ID = System.getenv("COGNITO_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("COGNITO_CLIENT_SECRET");
    private static final String POOL_ID = System.getenv("COGNITO_USER_POOL_ID");

    private final CognitoIdentityProviderClient cognitoClient;
    private final UserRepository userRepository;
    private final CognitoAuthService cognitoAuthService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, CognitoIdentityProviderClient cognitoClient, CognitoAuthService cognitoAuthService) {
        this.cognitoClient = cognitoClient;
        this.userRepository = userRepository;
        this.cognitoAuthService = cognitoAuthService;
    }

    public AuthResponse authenticate(String username, String password) {
        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", username);
        authParams.put("PASSWORD", password);
        authParams.put("SECRET_HASH", cognitoAuthService.calculateSecretHash(CLIENT_ID, CLIENT_SECRET, username));

        try {
            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .userPoolId(POOL_ID)
                    .clientId(CLIENT_ID)
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .authParameters(authParams)
                    .build();

            String idToken = getAccessToken(authRequest);
            User user = getUserById(idToken);

            return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), user.getActive(),
                    user.getBalance().getAmount(), idToken);
        } catch (CognitoIdentityProviderException e) {
            log.error("Error during authentication for user {}: {}", username, e.awsErrorDetails().errorMessage());
            throw new LoginFailedException("Authentication failed: " + e.awsErrorDetails().errorMessage());
        }
    }

    private String getAccessToken(AdminInitiateAuthRequest authRequest) {
        AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);
        return authResponse.authenticationResult().accessToken();
    }

    private User getUserById(String idToken) {
        DecodedJWT jwt = JWT.decode(idToken);
        String userId = jwt.getClaim("sub").asString();
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User was not found"));
    }
}

