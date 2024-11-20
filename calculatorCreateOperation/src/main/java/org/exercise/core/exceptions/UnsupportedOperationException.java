package org.exercise.core.exceptions;

public class UnsupportedOperationException extends RuntimeException {

    public UnsupportedOperationException(String message) {
        super(message);
    }

    public UnsupportedOperationException(String mainMessage, String secondaryMessage) {
        super(buildDetailedMessage(mainMessage, secondaryMessage));
    }

    private static String buildDetailedMessage(String mainMessage, String secondaryMessage) {
        return String.format("%s: %s", mainMessage, secondaryMessage);
    }
}