package org.exercise.core.services;

import org.exercise.core.entities.Operation;
import org.exercise.infrastructure.persistence.OperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OperationServiceImplTest {

    @Mock
    private OperationRepository operationRepository;

    @InjectMocks
    private OperationServiceImpl operationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetOperations() {
        // Arrange
        Operation operation1 = new Operation(); // Customize this object as necessary
        Operation operation2 = new Operation(); // Customize this object as necessary
        List<Operation> mockOperations = Arrays.asList(operation1, operation2);
        when(operationRepository.findAll()).thenReturn(mockOperations);

        // Act
        List<Operation> result = operationService.getOperations();

        // Assert
        assertEquals(2, result.size());
        assertEquals(mockOperations, result);
        verify(operationRepository, times(1)).findAll();
    }
}
