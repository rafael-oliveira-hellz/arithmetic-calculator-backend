package org.exercise.http.handlers;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.exercise.core.exceptions.InternalErrorException;
import org.exercise.core.exceptions.LoginFailedException;
import org.exercise.core.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> handleInternalErrorException(InternalErrorException ex) {
        logError(ex, "Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler({NoHandlerFoundException.class})
    public ResponseEntity<String> urlNotFound(Exception ex , HttpServletRequest request) {
        String body = "Requested url does not exist";
        logError(ex, body);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<String> notFound(Exception ex , HttpServletRequest request) {
        logError(ex, "Not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<String> methodNotAllowed(Exception ex , HttpServletRequest request) {
        logError(ex, "Method not allowed");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ex.getLocalizedMessage());
    }

    @ExceptionHandler({LoginFailedException.class})
    public ResponseEntity<String> loginFailed(Exception ex , HttpServletRequest request) {
        logError(ex, "Login failed");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ex.getLocalizedMessage());
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<String> conflict(Exception ex , HttpServletRequest request) {
        logError(ex, "Data integrity violation");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getLocalizedMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, DateTimeParseException.class,
            InvalidFormatException.class})
    public ResponseEntity<String> invalidAtribute(Exception ex, HttpServletRequest request) {
        String isolatedErrorMessage = getConstraintDefaultMessage(ex);
        logError(ex, "Unprocessable entity");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(isolatedErrorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        String message = "An unexpected error occurred: ";
        logError(ex, message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(message + ex.getLocalizedMessage());
    }

    String getConstraintDefaultMessage(Exception ex) {
        String errorMessage = ex.getLocalizedMessage();
        String startTag = "]]; default message [";
        String endTag = "]";
        int startIndex = errorMessage.indexOf(startTag) + startTag.length();
        int endIndex = errorMessage.indexOf(endTag, startIndex);
        return errorMessage.substring(startIndex, endIndex);
    }

    private static void logError(Exception ex, String message) {
        logger.error("{}: {}", message, ex.getMessage());
    }

}

