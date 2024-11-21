package org.exercise.http.controllers;

import org.exercise.core.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogoutControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private LogoutController logoutController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckStatus() {
        ResponseEntity<String> response = logoutController.checkStatus();

        assertNotNull(response);
        assertEquals("Status ok", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAuthenticateUser() {
        String accessToken = "mockAccessToken";

        ResponseEntity<LogoutController.Response> response = logoutController.authenticateUser(accessToken);

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).logoutUser(tokenCaptor.capture());
        assertEquals(accessToken, tokenCaptor.getValue());

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals("Logout successful!", response.getBody().message());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
