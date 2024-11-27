package org.exercise.core.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperationTypeTest {

    @Test
    void getName() {
        OperationType operationType = OperationType.ADDITION;

        assertEquals("addition", operationType.getName());
    }
}