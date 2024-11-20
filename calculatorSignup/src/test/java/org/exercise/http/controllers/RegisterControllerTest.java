package org.exercise.http.controllers;

import org.exercise.core.dtos.Register;
import org.exercise.core.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class RegisterControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private RegisterController registerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void checkStatus_shouldReturnStatusOk() {
        // Act
        ResponseEntity<String> response = registerController.checkStatus();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Status ok", response.getBody());
    }

    @Test
    void registerUser_shouldCallUserServiceAndReturnCreatedResponse() {
        Register dto = new Register("testUser", "Password123@", "test@example.com");

        ResponseEntity<RegisterController.Response> response = registerController.registerUser(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User created successfully!", response.getBody().message());
        verify(userService).registerUser(dto);
    }
}
