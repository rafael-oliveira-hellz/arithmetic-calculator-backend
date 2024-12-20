package org.exercise.http.handlers;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.exercise.core.dtos.ResponseTemplate;
import org.exercise.core.exceptions.BadGatewayException;
import org.exercise.core.exceptions.IllegalArgumentException;
import org.exercise.core.exceptions.InternalErrorException;
import org.exercise.core.exceptions.UnprocessableEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.format.DateTimeParseException;

@EnableWebMvc
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InternalErrorException.class)
    public ResponseEntity<ResponseTemplate> handleInternalErrorException(InternalErrorException ex) {
        logError(ex, ex.getLocalizedMessage());
        return createErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseTemplate> handleIllegalArgumentException(IllegalArgumentException ex) {
        logError(ex, ex.getLocalizedMessage());
        return createErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
    }

    @ExceptionHandler(BadGatewayException.class)
    public ResponseEntity<ResponseTemplate> handleBadGatewayException(BadGatewayException ex) {
        logError(ex, ex.getLocalizedMessage());
        return createErrorResponse(ex, HttpStatus.BAD_GATEWAY, ex.getLocalizedMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ResponseTemplate> handleNotFoundException(NoHandlerFoundException ex) {
        logError(ex, ex.getLocalizedMessage());
        return createErrorResponse(ex, HttpStatus.NOT_FOUND, ex.getLocalizedMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseTemplate> handleMethodNotAllowedException(HttpRequestMethodNotSupportedException ex) {
        logError(ex, ex.getLocalizedMessage());
        return createErrorResponse(ex, HttpStatus.METHOD_NOT_ALLOWED, ex.getLocalizedMessage());
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<ResponseTemplate> handleDataIntegrityViolationException(Exception ex) {
        logError(ex, "Data integrity violation");
        String errorMessage = "Unable to complete the operation due to a data integrity violation.";
        return createErrorResponse(ex, HttpStatus.CONFLICT, errorMessage);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, DateTimeParseException.class,
            InvalidFormatException.class, UnprocessableEntityException.class, JsonMappingException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<ResponseTemplate> handleUnprocessableEntityException(Exception ex) {
        String isolatedErrorMessage = getConstraintDefaultMessage(ex);
        logError(ex, ex.getLocalizedMessage());
        return createErrorResponse(ex, HttpStatus.UNPROCESSABLE_ENTITY, isolatedErrorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseTemplate> handleGeneralException(Exception ex) {
        logError(ex, ex.getLocalizedMessage());
        return createErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
    }

    private static ResponseEntity<ResponseTemplate> createErrorResponse(Exception ex, HttpStatus status, String message) {
        logError(ex, ex.getLocalizedMessage());
        return ResponseEntity.status(status).body(new ResponseTemplate(message));
    }

    private static void logError(Exception ex, String message) {
        logger.error("{}: {}", message, ex.getLocalizedMessage());
    }

    String getConstraintDefaultMessage(Exception ex) {
        try {
            String errorMessage = ex.getLocalizedMessage();
            String startTag = "]]; default message [";
            String endTag = "]";
            int startIndex = errorMessage.indexOf(startTag) + startTag.length();
            int endIndex = errorMessage.indexOf(endTag, startIndex);
            return errorMessage.substring(startIndex, endIndex);
        } catch (Exception e) {
            return ex.getLocalizedMessage();
        }
    }
}