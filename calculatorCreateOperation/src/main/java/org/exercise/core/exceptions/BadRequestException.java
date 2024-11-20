package org.exercise.core.exceptions;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String mainMessage, String secondaryMessage) {
        super(buildDetailedMessage(mainMessage, secondaryMessage));
    }

    private static String buildDetailedMessage(String mainMessage, String secondaryMessage) {
        return String.format("%s: %s", mainMessage, secondaryMessage);
    }
}