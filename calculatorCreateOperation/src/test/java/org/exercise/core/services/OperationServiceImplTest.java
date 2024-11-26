package org.exercise.core.services;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.exercise.core.entities.Balance;
import org.exercise.core.entities.Operation;
import org.exercise.core.entities.Record;
import org.exercise.core.entities.User;
import org.exercise.core.enums.OperationType;
import org.exercise.core.exceptions.BadRequestException;
import org.exercise.core.exceptions.NotFoundException;
import org.exercise.core.exceptions.PaymentRequiredException;
import org.exercise.core.exceptions.UnsupportedOperationException;
import org.exercise.infrastructure.clients.RandomStringClient;
import org.exercise.infrastructure.persistence.OperationRepository;
import org.exercise.infrastructure.persistence.RecordRepository;
import org.exercise.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        user = new User();
        user.setId(UUID.randomUUID());
        user.setBalance(new Balance(100));
        operation = new Operation(OperationType.ADDITION, 10);
    }

    @Test
    void testGetUserIdFromToken_InvalidFormat() {
        String token = "invalid.token";
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> operationService.doOperation(token, "ADDITION", "1.0", "1.0"));
        assertEquals("Invalid access token format: Invalid serialized unsecured/JWS/JWE object: Missing second delimiter", exception.getMessage());
    }

    @Test
    void testFindUserById_NotFound() throws ParseException {
        String token = "valid.token.here";
        UUID userId = UUID.randomUUID();

        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            SignedJWT signedJWT = mock(SignedJWT.class);
            JWTClaimsSet claimsSet = mock(JWTClaimsSet.class);

            mockedJWT.when(() -> SignedJWT.parse(token)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);
            when(claimsSet.getStringClaim("sub")).thenReturn(userId.toString());

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> operationService.doOperation(token, "ADDITION", "1.0", "1.0"));

            assertEquals("User not found. Try logging in again", exception.getMessage());
        }
    }

    @Test
    void testGetOperation_InvalidType() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> operationService.getOperation("INVALID", new BigDecimal("1.0"), new BigDecimal("1.0")));
        assertEquals("Invalid operation type: INVALID: No enum constant org.exercise.core.enums.OperationType.INVALID", exception.getMessage());
    }

    @Test
    void testCheckBalance_InsufficientFunds() {
        user.getBalance().setAmount(5);
        PaymentRequiredException exception = assertThrows(PaymentRequiredException.class,
                () -> operationService.checkBalance(user, 10));
        assertEquals("Insufficient User Balance for this operation", exception.getMessage());
    }

    @Test
    void testExecuteOperation_Addition_Value2Missing() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> operationService.executeOperation(OperationType.ADDITION, new BigDecimal("5.0"), null));
        assertEquals("The second value (value2) is required for operation: ADDITION", exception.getMessage());
    }

    @Test
    void testExecuteOperation_Subtraction_Value2Missing() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> operationService.executeOperation(OperationType.SUBTRACTION, new BigDecimal("5.0"), null));
        assertEquals("The second value (value2) is required for operation: SUBTRACTION", exception.getMessage());
    }

    @Test
    void testExecuteOperation_Multiplication_Value2Missing() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> operationService.executeOperation(OperationType.MULTIPLICATION, new BigDecimal("5.0"), null));
        assertEquals("The second value (value2) is required for operation: MULTIPLICATION", exception.getMessage());
    }

    @Test
    void testExecuteOperation_Division_Value2Missing() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> operationService.executeOperation(OperationType.DIVISION, new BigDecimal("5.0"), null));
        assertEquals("The second value (value2) is required for operation: DIVISION", exception.getMessage());
    }

    @Test
    void testExecuteOperation_Value1Missing() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> operationService.executeOperation(OperationType.ADDITION, null, new BigDecimal("5.0")));
        assertEquals("The first value (value1) is required for operation: ADDITION", exception.getMessage());
    }

    @Test
    void testExecuteOperation_Addition() {
        String result = operationService.executeOperation(OperationType.ADDITION, new BigDecimal("5.0"), new BigDecimal("5.0"));
        assertEquals("10", result);
    }

    @Test
    void testExecuteOperation_Subtraction() {
        String result = operationService.executeOperation(OperationType.SUBTRACTION, new BigDecimal("5.0"), new BigDecimal("5.0"));
        assertEquals("0", result);
    }

    @Test
    void testExecuteOperation_Multiplication() {
        String result = operationService.executeOperation(OperationType.MULTIPLICATION, new BigDecimal("5.0"), new BigDecimal("5.0"));
        assertEquals("25", result);
    }

    @Test
    void testExecuteOperation_DivisionByZero() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> operationService.executeOperation(OperationType.DIVISION, new BigDecimal("5.0"), BigDecimal.ZERO));
        assertEquals("Division by 0 is not possible", exception.getMessage());
    }

    @Test
    void testExecuteOperation_SquareRootNegative() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> operationService.executeOperation(OperationType.SQUARE_ROOT, new BigDecimal("-1.0"), null));
        assertEquals("Negative numbers don't have square roots", exception.getMessage());
    }

    @Test
    void testExecuteOperation_RandomString() {
        when(client.fetchRandomString(5)).thenReturn("random");
        String result = operationService.executeOperation(OperationType.RANDOM_STRING, new BigDecimal("5"), null);
        assertEquals("random", result);
        verify(client).fetchRandomString(5);
    }

    @Test
    void testSaveRecord_Success() {
        Record record = new Record();
        record.setId(UUID.randomUUID());

        when(recordRepository.save(any(Record.class))).thenReturn(record);

        Record result = operationService.saveRecord(operation, user, "result");

        assertNotNull(result);
        assertEquals(record.getId(), result.getId());
        verify(recordRepository).save(any(Record.class));
    }

    @Test
    void testDoOperation_Addition_Success() throws ParseException {
        String token = "valid.token.here";
        UUID userId = user.getId();
        String value1 = "10.5";
        String value2 = "20.3";

        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            SignedJWT signedJWT = mock(SignedJWT.class);
            JWTClaimsSet claimsSet = mock(JWTClaimsSet.class);

            mockedJWT.when(() -> SignedJWT.parse(token)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);
            when(claimsSet.getStringClaim("sub")).thenReturn(userId.toString());

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(operationRepository.findByType(OperationType.ADDITION)).thenReturn(Optional.of(operation));
            when(recordRepository.save(any(Record.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Record result = operationService.doOperation(token, "ADDITION", value1, value2);

            assertNotNull(result);
            assertEquals("30,8", result.getOperationResponse());
            assertEquals(user.getId(), result.getUser().getId());
        }
    }
}