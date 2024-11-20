package org.exercise.core.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.exercise.core.dtos.AuthResponse;
import org.exercise.core.entities.Balance;
import org.exercise.core.entities.User;
import org.exercise.core.exceptions.LoginFailedException;
import org.exercise.core.exceptions.NotFoundException;
import org.exercise.core.interfaces.CognitoAuthService;
import org.exercise.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CognitoAuthService cognitoAuthService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticate_success() {
        // Arrange
        String username = "testuser";
        String password = "password";
        String clientId = "testClientId";
        String clientSecret = "testSecret";
        String poolId = "testPoolId";
        String accessToken = "testAccessToken";

        System.setProperty("COGNITO_CLIENT_ID", clientId);
        System.setProperty("COGNITO_CLIENT_SECRET", clientSecret);
        System.setProperty("COGNITO_USER_POOL_ID", poolId);

        UUID userId = UUID.randomUUID();
        User user = new User(userId, username, "test@example.com", true, new Balance(1000));

        when(cognitoAuthService.calculateSecretHash(clientId, clientSecret, username))
                .thenReturn("testSecretHash");

        AuthenticationResultType authResult = mock(AuthenticationResultType.class);
        when(authResult.accessToken()).thenReturn(accessToken);

        AdminInitiateAuthResponse authResponse = mock(AdminInitiateAuthResponse.class);
        when(authResponse.authenticationResult()).thenReturn(authResult);
        when(cognitoClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class)))
                .thenReturn(authResponse);

        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);
        when(jwt.getClaim("sub")).thenReturn(claim);
        when(claim.as(UUID.class)).thenReturn(userId);

        mockStatic(JWT.class).when(() -> JWT.decode(accessToken)).thenReturn(jwt);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        AuthResponse response = userService.authenticate(username, password);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.id());
        assertEquals(username, response.username());
        verify(cognitoClient).adminInitiateAuth(any(AdminInitiateAuthRequest.class));
    }

    @Test
    void authenticate_userNotFound_throwsNotFoundException() {
        // Arrange
        String username = "testuser";
        String password = "password";
        String clientId = "testClientId";
        String clientSecret = "testSecret";
        String poolId = "testPoolId";
        String accessToken = "testAccessToken";

        System.setProperty("COGNITO_CLIENT_ID", clientId);
        System.setProperty("COGNITO_CLIENT_SECRET", clientSecret);
        System.setProperty("COGNITO_USER_POOL_ID", poolId);

        UUID userId = UUID.randomUUID();

        when(cognitoAuthService.calculateSecretHash(clientId, clientSecret, username))
                .thenReturn("testSecretHash");

        AuthenticationResultType authResult = mock(AuthenticationResultType.class);
        when(authResult.accessToken()).thenReturn(accessToken);

        AdminInitiateAuthResponse authResponse = mock(AdminInitiateAuthResponse.class);
        when(authResponse.authenticationResult()).thenReturn(authResult);
        when(cognitoClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class)))
                .thenReturn(authResponse);

        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);
        when(jwt.getClaim("sub")).thenReturn(claim);
        when(claim.as(UUID.class)).thenReturn(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> userService.authenticate(username, password));
    }

    @Test
    void authenticate_loginFailed_throwsLoginFailedException() {
        String username = "testuser";
        String password = "wrongpassword";

        when(cognitoClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class)))
                .thenThrow(new LoginFailedException("Authentication failed"));

        assertThrows(LoginFailedException.class, () -> userService.authenticate(username, password));
    }
}
