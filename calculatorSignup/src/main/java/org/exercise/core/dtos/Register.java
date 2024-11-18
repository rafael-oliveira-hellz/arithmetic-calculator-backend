package org.exercise.core.dtos;
import org.exercise.core.exceptions.UnprocessableEntityException;

import java.util.regex.Pattern;

public record Register(String username, String password, String email) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(\\.[A-Za-z]{2,})?$"
    );

    private static final String PASSWORD_MIN_LENGTH_ERROR = "Mínimo de 8 caracteres";
    private static final String PASSWORD_UPPERCASE_ERROR = "Pelo menos uma letra maiúscula";
    private static final String PASSWORD_LOWERCASE_ERROR = "Pelo menos uma letra minúscula";
    private static final String PASSWORD_NUMBER_ERROR = "Pelo menos um número";
    private static final String PASSWORD_SPECIAL_CHAR_ERROR = "Pelo menos um caractere especial";
    private static final String PASSWORD_NO_SPACES_OR_ACCENTED_ERROR = "Não conter espaços ou caracteres acentuados";

    public Register {
        validateEmail(email);
        validatePassword(password);
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new UnprocessableEntityException("O email não pode ser nulo ou vazio.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new UnprocessableEntityException("O email não está em um formato válido.");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new UnprocessableEntityException("A senha não pode ser nula ou vazia.");
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

