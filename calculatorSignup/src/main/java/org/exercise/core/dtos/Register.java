package org.exercise.core.dtos;
import org.exercise.core.exceptions.UnprocessableEntityException;

import java.util.regex.Pattern;

public record Register(String username, String password, String email) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(\\.[A-Za-z]{2,})?$"
    );

    private static final String PASSWORD_MIN_LENGTH_ERROR = "8 characters minimum";
    private static final String PASSWORD_UPPERCASE_ERROR = "At least one capital letter";
    private static final String PASSWORD_LOWERCASE_ERROR = "At least one lowercase letter";
    private static final String PASSWORD_NUMBER_ERROR = "At least one number";
    private static final String PASSWORD_SPECIAL_CHAR_ERROR = "At least one symbol";
    private static final String PASSWORD_NO_SPACES_OR_ACCENTED_ERROR = "No spaces or accented letters";

    public Register {
        validateEmail(email);
        validatePassword(password);
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new UnprocessableEntityException("Email can't be null nor empty");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new UnprocessableEntityException("Email format invalid.");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new UnprocessableEntityException("Password can't be null nor empty.");
        }

        if (password.length() < 8) {
            throw new UnprocessableEntityException(PASSWORD_MIN_LENGTH_ERROR);
        }
        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            throw new UnprocessableEntityException(PASSWORD_UPPERCASE_ERROR);
        }
        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            throw new UnprocessableEntityException(PASSWORD_LOWERCASE_ERROR);
        }
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            throw new UnprocessableEntityException(PASSWORD_NUMBER_ERROR);
        }
        if (!Pattern.compile("[^A-Za-z0-9]").matcher(password).find()) {
            throw new UnprocessableEntityException(PASSWORD_SPECIAL_CHAR_ERROR);
        }
        if (Pattern.compile("[\\sáàâãäéèêëíìîïóòôõöúùûüçñ]").matcher(password).find()) {
            throw new UnprocessableEntityException(PASSWORD_NO_SPACES_OR_ACCENTED_ERROR);
        }
    }
}

