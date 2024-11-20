package org.exercise.core.services;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.exercise.core.entities.Balance;
import org.exercise.core.entities.Operation;
import org.exercise.core.entities.Record;
import org.exercise.core.entities.User;
import org.exercise.core.enums.OperationType;
import org.exercise.core.exceptions.*;
import org.exercise.core.exceptions.UnsupportedOperationException;
import org.exercise.infrastructure.clients.RandomStringClient;
import org.exercise.infrastructure.persistence.OperationRepository;
import org.exercise.infrastructure.persistence.RecordRepository;
import org.exercise.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.text.ParseException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class OperationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private RandomStringClient client;

    @InjectMocks
    private OperationServiceImpl operationService;

    private User user;
    private Operation operation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(UUID.randomUUID());
        user.setBalance(new Balance(100));
        operation = new Operation(OperationType.ADDITION, 10);
    }

    @Test
    void testDoOperation_Success() throws ParseException {
        String token = "valid.token.here";
        String type = "ADDITION";
        Integer value1 = 5, value2 = 5;

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(operationRepository.findByType(any(OperationType.class))).thenReturn(Optional.of(operation));
        when(recordRepository.save(any(Record.class))).thenReturn(new Record());
        when(client.fetchRandomString(anyInt())).thenReturn("random");

        Record result = operationService.doOperation(token, type, value1, value2);

        assertNotNull(result);
        verify(userRepository).findById(any(UUID.class));
        verify(operationRepository).findByType(OperationType.ADDITION);
        verify(recordRepository).save(any(Record.class));
    }

    @Test
    void testGetUserIdFromToken_InvalidFormat() {
        String token = "invalid.token";
        assertThrows(BadRequestException.class, () -> operationService.doOperation(token, "ADDITION", 1, 1));
    }

    @Test
    void testFindUserById_NotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> operationService.doOperation("valid.token.here", "ADDITION", 1, 1));
    }

    @Test
    void testGetOperation_InvalidType() {
        assertThrows(IllegalArgumentException.class, () -> operationService.getOperation("INVALID", 1, 1));
    }

    @Test
    void testCheckBalance_InsufficientFunds() {
        user.getBalance().setAmount(5);
        assertThrows(PaymentRequiredException.class, () -> operationService.checkBalance(user, 10));
    }

    @Test
    void testExecuteOperation_Addition() {
        assertEquals("10", operationService.executeOperation(OperationType.ADDITION, 5, 5));
    }

    @Test
    void testExecuteOperation_Subtraction() {
        assertEquals("0", operationService.executeOperation(OperationType.SUBTRACTION, 5, 5));
    }

    @Test
    void testExecuteOperation_Multiplication() {
        assertEquals("25", operationService.executeOperation(OperationType.MULTIPLICATION, 5, 5));
    }

    @Test
    void testExecuteOperation_DivisionByZero() {
        assertThrows(UnsupportedOperationException.class, () -> operationService.executeOperation(OperationType.DIVISION, 5, 0));
    }

    @Test
    void testExecuteOperation_SquareRootNegative() {
        assertThrows(UnsupportedOperationException.class, () -> operationService.executeOperation(OperationType.SQUARE_ROOT, -1, null));
    }

    @Test
    void testExecuteOperation_RandomString() {
        when(client.fetchRandomString(5)).thenReturn("random");
        assertEquals("random", operationService.executeOperation(OperationType.RANDOM_STRING, 5, null));
    }

    @Test
    void testSaveRecord_Success() {
        Record record = new Record();
        when(recordRepository.save(record)).thenReturn(record);

        Record result = operationService.saveRecord(operation, user, "result");
        assertNotNull(result);
        verify(recordRepository).save(any(Record.class));
    }
}
