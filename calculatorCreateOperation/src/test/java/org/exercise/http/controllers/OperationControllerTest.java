package org.exercise.http.controllers;

import org.exercise.core.dtos.Values;
import org.exercise.core.entities.Operation;
import org.exercise.core.entities.Record;
import org.exercise.core.entities.User;
import org.exercise.core.interfaces.OperationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void checkStatus_shouldReturnStatusOk() {
        // Act
        ResponseEntity<String> response = operationController.checkStatus();

        // Assert
        assertEquals("Status ok", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void registerUser_shouldReturnRecordResponse() {
        String accessToken = "dummyAccessToken";
        String type = "exampleType";
        Values values = new Values(BigDecimal.TEN, BigDecimal.valueOf(20));

        Operation mockOperation = new Operation();
        User mockUser = new User();
        Integer amount = 10;
        Integer userBalance = 90;
        String operationResponse = "Success";

        Record expectedRecord = new Record(mockOperation, mockUser, amount, userBalance, operationResponse);

        when(operationService.doOperation(accessToken, type, values.value1(), values.value2()))
                .thenReturn(expectedRecord);

        ResponseEntity<Record> response = operationController.registerUser(accessToken, type, values);

        assertEquals(expectedRecord, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(operationService, times(1)).doOperation(accessToken, type, values.value1(), values.value2());
    }
}
