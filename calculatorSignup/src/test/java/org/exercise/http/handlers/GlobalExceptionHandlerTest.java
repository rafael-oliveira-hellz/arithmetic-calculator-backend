package org.exercise.http.handlers;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.exercise.core.dtos.ResponseTemplate;
import org.exercise.core.exceptions.BadGatewayException;
import org.exercise.core.exceptions.IllegalArgumentException;
import org.exercise.core.exceptions.InternalErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;

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
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleInternalErrorException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal error", response.getBody().message());
    }

    @Test
    void handleIllegalArgumentException_shouldReturnBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleIllegalArgumentException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid argument", response.getBody().message());
    }

    @Test
    void handleBadGatewayException_shouldReturnBadGateway() {
        BadGatewayException ex = new BadGatewayException("Bad gateway");
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleBadGatewayException(ex);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("Bad gateway", response.getBody().message());
    }

    @Test
    void handleMethodNotAllowedException_shouldReturnMethodNotAllowed() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleMethodNotAllowedException(ex);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("Request method 'POST' is not supported", response.getBody().message());
    }

    @Test
    void handleDataIntegrityViolationException_shouldReturnConflict() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Data conflict");
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleDataIntegrityViolationException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Unable to complete the operation due to a data integrity violation.", response.getBody().message());
    }

    @Test
    void handleInvalidAttributeException_shouldReturnInvalidAtributeEntity() {
        InvalidFormatException ex = mock(InvalidFormatException.class);
        String mockMessage = "Error occurred]]; default message [Invalid value]";
        org.mockito.Mockito.when(ex.getLocalizedMessage()).thenReturn(mockMessage);
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleUnprocessableEntityException(ex);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Invalid value", response.getBody().message());
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
        org.mockito.Mockito.when(ex.getLocalizedMessage()).thenReturn(errorMessage);
        String extractedMessage = exceptionHandler.getConstraintDefaultMessage(ex);
        assertEquals("Invalid input", extractedMessage);
    }
}