package org.exercise.interfaces.exceptions;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;

public class InternalErrorException extends RuntimeException {

    public InternalErrorException(String message) {
        super(message);
    }
}
