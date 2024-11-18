package org.exercise.core.exceptions;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;

public class IllegalArgumentException extends RuntimeException {

    public IllegalArgumentException(String message) {
        super(message);
    }
}
