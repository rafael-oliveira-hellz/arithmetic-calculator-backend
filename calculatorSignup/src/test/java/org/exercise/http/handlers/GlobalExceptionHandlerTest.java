package org.exercise.http.handlers;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.exercise.core.exceptions.BadGatewayException;
import org.exercise.core.exceptions.IllegalArgumentException;
import org.exercise.core.exceptions.InternalErrorException;
import org.exercise.core.exceptions.UnprocessableEntityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleInternalErrorException_shouldReturnIllegalArgument() {
        InternalErrorException ex = new InternalErrorException("Internal error");
        ResponseEntity<String> response = exceptionHandler.handleIllegalArgumentException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal error", response.getBody());
    }

    @Test
    void handleIllegalArgumentException_shouldReturnBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        ResponseEntity<String> response = exceptionHandler.handleIllegalArgumentException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid argument", response.getBody());
    }

    @Test
    void badGatewayException_shouldReturnBadGateway() {
        BadGatewayException ex = new BadGatewayException("Bad gateway");
        ResponseEntity<String> response = exceptionHandler.badGatewayException(ex);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("Bad gateway", response.getBody());
    }

    @Test
    void methodNotAllowed_shouldReturnMethodNotAllowed() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");
        ResponseEntity<String> response = exceptionHandler.methodNotAllowed(ex, request);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("Request method 'POST' is not supported", response.getBody());
    }

    @Test
    void conflict_shouldReturnConflict() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Data conflict");
        ResponseEntity<String> response = exceptionHandler.conflict(ex, request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Data conflict", response.getBody());
    }

    @Test
    void invalidAtribute_shouldReturnInvalidAtributeEntity() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        InvalidFormatException ex = mock(InvalidFormatException.class);
        String mockMessage = "Error occurred]]; default message [Invalid value]";
        org.mockito.Mockito.when(ex.getLocalizedMessage()).thenReturn(mockMessage);
        ResponseEntity<String> response = exceptionHandler.invalidAtribute(ex, request);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Invalid value", response.getBody());
    }

    @Test
    void handleGeneralException_shouldReturnInternalServerError() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<String> response = exceptionHandler.handleGeneralException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred: Unexpected error", response.getBody());
    }

    @Test
    void getConstraintDefaultMessage_shouldExtractErrorMessage() {
        String errorMessage = "Error occurred]]; default message [Invalid input]";
        Exception ex = mock(Exception.class);
        org.mockito.Mockito.when(ex.getLocalizedMessage()).thenReturn(errorMessage);
        String extractedMessage = exceptionHandler.getConstraintDefaultMessage(ex);
        assertEquals("Invalid input", extractedMessage);
    }
}
