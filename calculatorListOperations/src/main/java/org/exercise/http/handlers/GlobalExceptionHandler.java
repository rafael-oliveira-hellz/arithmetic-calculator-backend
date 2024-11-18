package org.exercise.http.handlers;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.exercise.core.exceptions.InternalErrorException;
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

    @ExceptionHandler(InternalErrorException.class)
    public ResponseEntity<String> handleInternalErrorException(InternalErrorException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler({NoHandlerFoundException.class})
    public ResponseEntity<String> urlNotFound(Exception ex , HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("A url requisitada não existe. Por favor confira se não houve erro de digitação");
    }
    
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<String> methodNotAllowed(Exception ex , HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ex.getLocalizedMessage());
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<String> conflict(Exception ex , HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getLocalizedMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, DateTimeParseException.class,
            InvalidFormatException.class})
    public ResponseEntity<String> invalidAtribute(Exception ex, HttpServletRequest request) {
        String isolatedErrorMessage = getConstraintDefaultMessage(ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(isolatedErrorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + ex.getLocalizedMessage());
    }

    private String getConstraintDefaultMessage(Exception ex) {
        String errorMessage = ex.getLocalizedMessage();
        String startTag = "]]; default message [";
        String endTag = "]";
        int startIndex = errorMessage.indexOf(startTag) + startTag.length();
        int endIndex = errorMessage.indexOf(endTag, startIndex);
        return errorMessage.substring(startIndex, endIndex);
    }

}

