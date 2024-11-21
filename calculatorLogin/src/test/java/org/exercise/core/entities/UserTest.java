package org.exercise.core.entities;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserUsingDefaultConstructor() {
        User user = new User();

        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getEmail());
        assertNull(user.getBalance());
        assertTrue(user.getActive());
    }

    @Test
    void shouldCreateUserUsingParameterizedConstructor() {
        UUID id = UUID.randomUUID();
        String username = "testUser";
        String password = "securePassword";
        String email = "test@example.com";
        Balance balance = new Balance();

        User user = new User(id, username, password, email, true, balance);


        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        assertEquals(email, user.getEmail());
        assertEquals(balance, user.getBalance());
        assertTrue(user.getActive());
    }

    @Test
    void shouldSetAndGetId() {
        User user = new User();
        UUID id = UUID.randomUUID();

        user.setId(id);

        assertEquals(id, user.getId());
    }

    @Test
    void shouldSetAndGetUsername() {
        User user = new User();
        String username = "testUser";

        user.setUsername(username);

        assertEquals(username, user.getUsername());
    }

    @Test
    void shouldSetAndGetPassword() {
        User user = new User();
        String password = "securePassword";

        user.setPassword(password);

        assertEquals(password, user.getPassword());
    }

    @Test
    void shouldSetAndGetEmail() {
        User user = new User();
        String email = "test@example.com";

        user.setEmail(email);

        assertEquals(email, user.getEmail());
    }

    @Test
    void shouldSetAndGetActive() {
        User user = new User();

        user.setActive(false);

        assertFalse(user.getActive());
    }

    @Test
    void shouldSetAndGetBalance() {
        User user = new User();
        Balance balance = new Balance();

        user.setBalance(balance);

        assertEquals(balance, user.getBalance());
    }
}
