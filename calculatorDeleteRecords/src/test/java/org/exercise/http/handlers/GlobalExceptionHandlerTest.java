package org.exercise.http.handlers;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.exercise.core.dtos.ResponseTemplate;
import org.exercise.core.exceptions.ForbiddenException;
import org.exercise.core.exceptions.InternalErrorException;
import org.exercise.core.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

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
        InternalErrorException ex = new InternalErrorException("Internal error");
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleInternalErrorException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal error", response.getBody().message());
    }

    @Test
    void handleForbiddenException_shouldReturnForbidden() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        ForbiddenException ex = new ForbiddenException("Forbidden");
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleForbiddenException(ex, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden", response.getBody().message());
    }

    @Test
    void handleNotFoundException_shouldReturnNotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        NotFoundException ex = new NotFoundException("Not found");
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().message());
    }

    @Test
    void handleUrlNotFoundException_shouldReturnNotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/test", null);
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleUrlNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No endpoint GET /test.", response.getBody().message());
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

        org.hibernate.exception.ConstraintViolationException hibernateEx =
                mock(org.hibernate.exception.ConstraintViolationException.class);
        when(hibernateEx.getConstraintName()).thenReturn("unique_constraint");

        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("Unique constraint violation", hibernateEx);

        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(
                Map.of("status", 409, "error", "Conflict", "message", "Unique constraint violated"),
                response.getBody()
        );
    }


    @Test
    void handleDataIntegrityViolationException_shouldReturnUnprocessableEntity() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        org.hibernate.exception.ConstraintViolationException hibernateEx =
                mock(org.hibernate.exception.ConstraintViolationException.class);
        when(hibernateEx.getConstraintName()).thenReturn("foreign_key_constraint");

        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("Foreign key constraint violation", hibernateEx);

        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals(
                Map.of("status", 422, "error", "Unprocessable Entity", "message", "Foreign key constraint violated"),
                response.getBody()
        );
    }

    @Test
    void handleDataIntegrityViolationException_shouldReturnBadRequestForGenericCase() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Generic data integrity violation");

        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                Map.of("status", 400, "error", "Bad Request", "message", "Generic data integrity violation"),
                response.getBody()
        );
    }

    @Test
    void handleInvalidAttributeException_shouldReturnUnprocessableEntity() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        InvalidFormatException ex = mock(InvalidFormatException.class);
        String mockMessage = "Error occurred]]; default message [Invalid value]";
        when(ex.getLocalizedMessage()).thenReturn(mockMessage);
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleInvalidAttributeException(ex);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Invalid value", response.getBody().message());
    }

    @Test
    void handleGeneralException_shouldReturnInternalServerError() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<ResponseTemplate> response = exceptionHandler.handleGeneralException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred: Unexpected error", response.getBody().message());
    }
}
