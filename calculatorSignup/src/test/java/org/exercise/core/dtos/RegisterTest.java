package org.exercise.core.dtos;

import org.exercise.core.exceptions.UnprocessableEntityException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterTest {

    @Test
    void shouldCreateRegisterWithValidEmailAndPassword() {
        assertDoesNotThrow(() -> new Register("validUser", "Valid1@pw", "test@example.com"));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,
                () -> new Register("user", "Valid1@", null));
        assertEquals("Email can't be null nor empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,
                () -> new Register("user", "Valid1@", ""));
        assertEquals("Email can't be null nor empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,
                () -> new Register("user", "Valid1@", "invalid-email"));
        assertEquals("Email format invalid.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsNull() {
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,
                () -> new Register("user", null, "test@example.com"));
        assertEquals("Password can't be null nor empty.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsBlank() {
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,
                () -> new Register("user", "", "test@example.com"));
        assertEquals("Password can't be null nor empty.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsTooShort() {
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,
                () -> new Register("user", "Short1", "test@example.com"));
        assertEquals("8 characters minimum", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordLacksUppercase() {
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,
                () -> new Register("user", "lowercase1@", "test@example.com"));
        assertEquals("At least one capital letter", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordLacksLowercase() {
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,
                () -> new Register("user", "UPPERCASE1@", "test@example.com"));
        assertEquals("At least one lowercase letter", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordLacksNumber() {
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,
                () -> new Register("user", "NoNumber@", "test@example.com"));
        assertEquals("At least one number", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordLacksSpecialCharacter() {
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,
                () -> new Register("user", "NoSpecial1", "test@example.com"));
        assertEquals("At least one symbol", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordContainsSpaceOrAccentedCharacters() {
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,
                () -> new Register("user", "Invalid 1@", "test@example.com"));
        assertEquals("No spaces or accented letters", exception.getMessage());

        exception = assertThrows(UnprocessableEntityException.class,
                () -> new Register("user", "Inválido1@", "test@example.com"));
        assertEquals("No spaces or accented letters", exception.getMessage());
    }

    @Test
    void shouldCreateRegisterWithValidInputs() {
        String validUsername = "validUser";
        String validPassword = "Valid1@pw";
        String validEmail = "test@example.com";

        Register register = new Register(validUsername, validPassword, validEmail);

        assertEquals(validUsername, register.username());
        assertEquals(validPassword, register.password());
        assertEquals(validEmail, register.email());
    }
}
