package org.exercise.http.handlers;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.exercise.core.dtos.ResponseTemplate;
import org.exercise.core.exceptions.InternalErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleInternalErrorException_shouldReturnInternalServerError() {
        InternalErrorException ex = new InternalErrorException("Internal error", null);
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleInternalErrorException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal error", response.getBody().message());
    }

    @Test
    void handleUrlNotFoundException_shouldReturnNotFound() {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/test", null);
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleUrlNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No endpoint GET /test.", response.getBody().message());
    }

    @Test
    void handleMethodNotAllowedException_shouldReturnMethodNotAllowed() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleMethodNotAllowedException(ex);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("Request method 'POST' is not supported", response.getBody().message());
    }

    @Test
    void handleInvalidAttributeException_shouldReturnInvalidAtributeEntity() {
        InvalidFormatException ex = mock(InvalidFormatException.class);
        String mockMessage = "Error occurred]]; default message [Invalid value]";
        when(ex.getLocalizedMessage()).thenReturn(mockMessage);
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleInvalidAttributeException(ex);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Invalid value", response.getBody().message());
    }

    @Test
    void handleIllegalArgumentException_shouldReturnUnprocessableEntity() {
        IllegalArgumentException ex = new IllegalArgumentException("Illegal argument");
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleIllegalArgumentException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Illegal argument", response.getBody().message());
    }

    @Test
    void handleGeneralException_shouldReturnInternalServerError() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleGeneralException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody().message());
    }

    @Test
    void getConstraintDefaultMessage_shouldExtractErrorMessage() {
        String errorMessage = "Error occurred]]; default message [Invalid input]";
        Exception ex = mock(Exception.class);
        when(ex.getLocalizedMessage()).thenReturn(errorMessage);
        String extractedMessage = exceptionHandler.getConstraintDefaultMessage(ex);
        assertEquals("Invalid input", extractedMessage);
    }
}
