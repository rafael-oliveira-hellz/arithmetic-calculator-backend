package org.exercise.core.entities;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RecordTest {

    @Test
    void testRecordGettersAndSetters() {
        Record recordObject = new Record();

        UUID id = UUID.randomUUID();
        Operation operation = new Operation();
        User user = new User();
        int amount = 500;
        int userBalance = 1500;
        String operationResponse = "Success";
        ZonedDateTime date = ZonedDateTime.now();
        boolean deleted = true;

        recordObject.setId(id);
        recordObject.setOperation(operation);
        recordObject.setUser(user);
        recordObject.setAmount(amount);
        recordObject.setUserBalance(userBalance);
        recordObject.setOperationResponse(operationResponse);
        recordObject.setDate(date);
        recordObject.setDeleted(deleted);

        assertEquals(id, recordObject.getId());
        assertEquals(operation, recordObject.getOperation());
        assertEquals(user, recordObject.getUser());
        assertEquals(amount, recordObject.getAmount());
        assertEquals(userBalance, recordObject.getUserBalance());
        assertEquals(operationResponse, recordObject.getOperationResponse());
        assertEquals(date, recordObject.getDate());
        assertEquals(deleted, recordObject.getDeleted());
    }

    @Test
    void testRecordDefaultConstructor() {
        Record recordObject = new Record();

        assertNull(recordObject.getId());
        assertNull(recordObject.getOperation());
        assertNull(recordObject.getUser());
        assertNull(recordObject.getAmount());
        assertNull(recordObject.getUserBalance());
        assertNull(recordObject.getOperationResponse());
        assertNotNull(recordObject.getDate());
        assertFalse(recordObject.getDeleted());
    }

}
