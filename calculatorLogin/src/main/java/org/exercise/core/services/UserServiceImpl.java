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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private static final String CLIENT_ID = System.getenv("COGNITO_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("COGNITO_CLIENT_SECRET");
    private static final String POOL_ID = System.getenv("COGNITO_USER_POOL_ID");

    private final CognitoIdentityProviderClient cognitoClient;
    private final UserRepository userRepository;
    private final CognitoAuthService cognitoAuthService;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    public UserServiceImpl(UserRepository userRepository, CognitoIdentityProviderClient cognitoClient, CognitoAuthService cognitoAuthService) {
        this.cognitoClient = cognitoClient;
        this.userRepository = userRepository;
        this.cognitoAuthService = cognitoAuthService;
    }

    public AuthResponse authenticate(String username, String password) {
        logger.info("Setting authentication params for user : {}", username);
        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", username);
        authParams.put("PASSWORD", password);
        authParams.put("SECRET_HASH", cognitoAuthService.calculateSecretHash(CLIENT_ID, CLIENT_SECRET, username));

        try {
            logger.info("Building authentication request");
            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .userPoolId(POOL_ID)
                    .clientId(CLIENT_ID)
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .authParameters(authParams)
                    .build();

            logger.info("Authenticating user at cognito");
            String accessToken = getAccessToken(authRequest);

            logger.info("Authentication successful. Access token retrieved");
            User user = getUserById(accessToken);

            logger.info("User found. Username: {}", user.getUsername());
            return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), user.getActive(),
                    user.getBalance().getAmount(), accessToken);
        } catch (CognitoIdentityProviderException e) {
            logger.error("Error during authentication for user {}: {}", username, e.awsErrorDetails().errorMessage());
            throw new LoginFailedException("Authentication failed: " + e.awsErrorDetails().errorMessage());
        }
    }

    private String getAccessToken(AdminInitiateAuthRequest authRequest) {
        AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);
        return authResponse.authenticationResult().accessToken();
    }

    private User getUserById(String idToken) {
        logger.info("Retrieving user id from access token");
        DecodedJWT jwt = JWT.decode(idToken);
        UUID userId = jwt.getClaim("sub").as(UUID.class);
        logger.info("User id found: {}", userId);
        return userRepository.findByIdAndActive(userId, true).orElseThrow(() -> new NotFoundException("User was not found"));
    }
}

