package org.exercise.core.exceptions;

public class PaymentRequiredException extends RuntimeException {

    public PaymentRequiredException(String message) {
        super(message);
    }
}
