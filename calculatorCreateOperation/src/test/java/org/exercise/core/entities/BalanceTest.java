package org.exercise.core.entities;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BalanceTest {

    @Test
    void shouldCreateBalanceUsingDefaultConstructor() {
        Balance balance = new Balance();

        assertNotNull(balance);
        assertNull(balance.getId());
        assertNull(balance.getAmount());
    }

    @Test
    void shouldCreateBalanceUsingParameterizedConstructor() {
        Integer amount = 100;

        Balance balance = new Balance();
        balance.setAmount(amount);

        assertNotNull(balance);
        assertNull(balance.getId()); // ID is not set in constructor
        assertEquals(amount, balance.getAmount());
    }

    @Test
    void shouldSetAndGetId() {
        Balance balance = new Balance();
        UUID id = UUID.randomUUID();

        balance.setId(id);

        assertEquals(id, balance.getId());
    }

    @Test
    void shouldSetAndGetAmount() {
        Balance balance = new Balance();
        Integer amount = 500;

        balance.setAmount(amount);

        assertEquals(amount, balance.getAmount());
    }
}
