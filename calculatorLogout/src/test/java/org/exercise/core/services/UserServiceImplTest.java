package org.exercise.core.services;

import org.exercise.config.CognitoConfig;
import org.exercise.core.exceptions.LogoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GlobalSignOutRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private CognitoConfig cognitoConfig;

    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(cognitoConfig.cognitoClient()).thenReturn(cognitoClient);
        userService = new UserServiceImpl(cognitoConfig);
    }

    @Test
    void logoutUser_successful() {
        // Arrange
        String accessToken = "dummyAccessToken";
        GlobalSignOutRequest expectedRequest = GlobalSignOutRequest.builder()
                .accessToken(accessToken)
                .build();

        // Act & Assert
        assertDoesNotThrow(() -> userService.logoutUser(accessToken));

        // Verify the expected interactions
        verify(cognitoClient).globalSignOut(expectedRequest);
        verifyNoMoreInteractions(cognitoClient);
    }

    @Test
    void logoutUser_cognitoException() {
        // Arrange
        String accessToken = "dummyAccessToken";
        CognitoIdentityProviderException cognitoException = (CognitoIdentityProviderException) CognitoIdentityProviderException.builder()
                .message("Invalid token")
                .awsErrorDetails(software.amazon.awssdk.awscore.exception.AwsErrorDetails.builder()
                        .errorMessage("Invalid token")
                        .build())
                .build();

        doThrow(cognitoException).when(cognitoClient).globalSignOut(any(GlobalSignOutRequest.class));

        // Act & Assert
        LogoutException exception = assertThrows(LogoutException.class, () -> userService.logoutUser(accessToken));
        assertEquals("Logout failed: Invalid token", exception.getMessage());

        // Verify the expected interactions
        verify(cognitoClient).globalSignOut(any(GlobalSignOutRequest.class));
    }

    @Test
    void logoutUser_illegalStateException() {
        // Arrange
        String accessToken = "dummyAccessToken";
        doThrow(new IllegalStateException("Connection pool shut down")).when(cognitoClient)
                .globalSignOut(any(GlobalSignOutRequest.class));

        // Act & Assert
        LogoutException exception = assertThrows(LogoutException.class, () -> userService.logoutUser(accessToken));
        assertEquals("Cognito client is unavailable. Please try again later.", exception.getMessage());

        // Verify the expected interactions
        verify(cognitoClient).globalSignOut(any(GlobalSignOutRequest.class));
    }

    @Test
    void logoutUser_unexpectedException() {
        // Arrange
        String accessToken = "dummyAccessToken";
        doThrow(new RuntimeException("Unexpected error")).when(cognitoClient)
                .globalSignOut(any(GlobalSignOutRequest.class));

        // Act & Assert
        LogoutException exception = assertThrows(LogoutException.class, () -> userService.logoutUser(accessToken));
        assertEquals("Unexpected error occurred during logout.", exception.getMessage());

        // Verify the expected interactions
        verify(cognitoClient).globalSignOut(any(GlobalSignOutRequest.class));
    }

    @Test
    void maskToken_validToken() {
        // Arrange
        String token = "123456789012345";

        // Act
        String maskedToken = userService.maskToken(token);

        // Assert
        assertEquals("12345******12345", maskedToken);
    }

    @Test
    void maskToken_nullToken() {
        // Act
        String maskedToken = userService.maskToken(null);

        // Assert
        assertEquals("N/A", maskedToken);
    }

    @Test
    void maskToken_shortToken() {
        // Arrange
        String token = "12345";

        // Act
        String maskedToken = userService.maskToken(token);

        // Assert
        assertEquals("N/A", maskedToken);
    }
}
