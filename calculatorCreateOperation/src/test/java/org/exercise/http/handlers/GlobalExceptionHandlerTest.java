package org.exercise.http.handlers;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.exercise.core.exceptions.*;
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
    void handleInternalErrorException_shouldReturnInternalServerError() {
        InternalErrorException ex = new InternalErrorException("Internal error");
        ResponseEntity<String> response = exceptionHandler.handleInternalErrorException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal error", response.getBody());
    }

    @Test
    void handleBadRequestException_shouldReturnBadRequestError() {
        BadRequestException ex = new BadRequestException("Bad request");
        ResponseEntity<String> response = exceptionHandler.badRequestException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request", response.getBody());
    }

    @Test
    void handlePaymentRequiredException_shouldReturnPaymentRequiredError() {
        PaymentRequiredException ex = new PaymentRequiredException("Payment required");
        ResponseEntity<String> response = exceptionHandler.paymentRequiredException(ex);
        assertEquals(HttpStatus.PAYMENT_REQUIRED, response.getStatusCode());
        assertEquals("Payment required", response.getBody());
    }

    @Test
    void handleBadGatewayException_shouldReturnBadGatewayError() {
        BadGatewayException ex = new BadGatewayException("Bad gateway");
        ResponseEntity<String> response = exceptionHandler.badGateway(ex, null);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("Bad gateway", response.getBody());
    }

    @Test
    void handleNotFoundException_shouldReturnNotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        NotFoundException ex = new NotFoundException("Not found");
        ResponseEntity<String> response = exceptionHandler.notFound(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody());
    }

    @Test
    void urlNotFound_shouldReturnNotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/test", null);
        ResponseEntity<String> response = exceptionHandler.urlNotFound(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("A url requisitada não existe. Por favor confira se não houve erro de digitação", response.getBody());
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
    void invalidAtribute_shouldReturnUnprocessableEntity() {
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
}
