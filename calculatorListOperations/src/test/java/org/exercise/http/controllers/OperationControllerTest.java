package org.exercise.http.controllers;

import org.exercise.core.entities.Operation;
import org.exercise.core.interfaces.OperationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OperationControllerTest {

    @Mock
    private OperationService operationService;

    @InjectMocks
    private OperationController operationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckStatus() {
        ResponseEntity<String> response = operationController.checkStatus();

        assertNotNull(response);
        assertEquals("Status ok", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetOperations() {
        Operation operation1 = new Operation(); // Customize as needed
        Operation operation2 = new Operation(); // Customize as needed
        List<Operation> mockOperations = Arrays.asList(operation1, operation2);
        when(operationService.getOperations()).thenReturn(mockOperations);

        ResponseEntity<List<Operation>> response = operationController.getOperations();

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(mockOperations, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(operationService, times(1)).getOperations();
    }

}
