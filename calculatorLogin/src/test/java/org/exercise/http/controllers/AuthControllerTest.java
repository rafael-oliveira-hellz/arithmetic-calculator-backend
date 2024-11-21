package org.exercise.http.controllers;

import org.exercise.core.dtos.AuthDto;
import org.exercise.core.dtos.AuthResponse;
import org.exercise.core.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private AuthController authController;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(userService);
    }

    @Test
    void checkStatusShouldReturnStatusOk() {
        // Act
        ResponseEntity<String> response = authController.checkStatus();

        // Assert
        assertEquals("Status ok", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void authenticateUserShouldReturnAuthResponse() {
        // Arrange
        AuthDto authDto = new AuthDto("testUser", "password123");
        AuthResponse mockResponse = new AuthResponse(UUID.randomUUID(), "testUser", "testUser@email.com",
                true, 100, "testAccessToken");
        when(userService.authenticate(authDto.username(), authDto.password())).thenReturn(mockResponse);

        ResponseEntity<AuthResponse> response = authController.authenticateUser(authDto);

        assertEquals(mockResponse, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).authenticate(authDto.username(), authDto.password());
    }
}
