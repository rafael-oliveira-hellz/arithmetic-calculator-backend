package org.exercise.core.entities;

import jakarta.persistence.*;
import org.exercise.core.enums.OperationType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OperationTest {

    @Test
    void testOperationGettersAndSetters() {
        Operation operation = new Operation();

        UUID id = UUID.randomUUID();
        OperationType type = OperationType.ADDITION;
        int cost = 100;

        operation.setId(id);
        operation.setType(type);
        operation.setCost(cost);

        assertEquals(id, operation.getId());
        assertEquals(type, operation.getType());
        assertEquals(0, operation.getType().getId());
        assertEquals(cost, operation.getCost());
    }

    @Test
    void testOperationDefaultConstructor() {
        Operation operation = new Operation();

        assertNull(operation.getId());
        assertNull(operation.getType());
        assertNull(operation.getCost());
    }

    @Test
    void testAnnotations() {
        assertTrue(Operation.class.isAnnotationPresent(Entity.class));

        Table tableAnnotation = Operation.class.getAnnotation(Table.class);
        assertNotNull(tableAnnotation);
        assertEquals("tb_operations", tableAnnotation.name());

        try {
            assertTrue(Operation.class.getDeclaredField("id").isAnnotationPresent(Id.class));
        } catch (NoSuchFieldException e) {
            fail("Field 'id' should be annotated with @Id");
        }

        try {
            Enumerated enumeratedAnnotation = Operation.class.getDeclaredField("type").getAnnotation(Enumerated.class);
            assertNotNull(enumeratedAnnotation);
            assertEquals(EnumType.STRING, enumeratedAnnotation.value());
        } catch (NoSuchFieldException e) {
            fail("Field 'type' should be annotated with @Enumerated");
        }
    }
}
