package org.exercise.core.services;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.exercise.core.entities.Balance;
import org.exercise.core.entities.Record;
import org.exercise.core.entities.Operation;
import org.exercise.core.entities.User;
import org.exercise.core.enums.OperationType;
import org.exercise.core.exceptions.BadRequestException;
import org.exercise.core.exceptions.NotFoundException;
import org.exercise.core.exceptions.PaymentRequiredException;
import org.exercise.core.exceptions.UnsupportedOperationException;
import org.exercise.core.interfaces.OperationService;
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
    private BigDecimal value1;
    private BigDecimal value2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setBalance(new Balance(100));
        value1 = BigDecimal.valueOf(10);
        value2 = BigDecimal.valueOf(20);
    }

    @Test
    void testDoOperation_Success() throws ParseException {
        String token = "valid.token.here";
        UUID userId = UUID.randomUUID();
        String type = "ADDITION";

        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            SignedJWT signedJWT = mock(SignedJWT.class);
            JWTClaimsSet claimsSet = mock(JWTClaimsSet.class);

            mockedJWT.when(() -> SignedJWT.parse(token)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);
            when(claimsSet.getStringClaim("sub")).thenReturn(userId.toString());

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

            Operation additionOperation = new Operation(OperationType.ADDITION, 10);
            when(operationRepository.findByType(OperationType.ADDITION)).thenReturn(Optional.of(additionOperation));

            when(recordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Record result = operationService.doOperation(token, type, value1, value2);

            assertNotNull(result);
            assertEquals(OperationType.ADDITION, result.getOperation().getType());
            assertEquals("30", result.getOperationResponse()); // Resultado da soma
            assertEquals(90, result.getUser().getBalance().getAmount()); // Saldo reduzido
            verify(userRepository).findById(userId);
            verify(operationRepository).findByType(OperationType.ADDITION);
            verify(recordRepository).save(any());
        }
    }

    @Test
    void testDoOperation_InvalidToken() {
        String token = "invalid.token";
        String type = "ADDITION";

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> operationService.doOperation(token, type, value1, value2));
        assertEquals("Invalid access token format: Invalid serialized unsecured/JWS/JWE object: Missing second delimiter", exception.getMessage());
    }

    @Test
    void testDoOperation_UserNotFound() throws ParseException {
        String token = "valid.token.here";
        UUID userId = UUID.randomUUID();
        String type = "ADDITION";

        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            SignedJWT signedJWT = mock(SignedJWT.class);
            JWTClaimsSet claimsSet = mock(JWTClaimsSet.class);

            mockedJWT.when(() -> SignedJWT.parse(token)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);
            when(claimsSet.getStringClaim("sub")).thenReturn(userId.toString());

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> operationService.doOperation(token, type, value1, value2));
            assertEquals("User not found. Try logging in again", exception.getMessage());
        }
    }

    @Test
    void testDoOperation_InsufficientFunds() throws ParseException {
        String token = "valid.token.here";
        UUID userId = UUID.randomUUID();
        String type = "ADDITION";

        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            SignedJWT signedJWT = mock(SignedJWT.class);
            JWTClaimsSet claimsSet = mock(JWTClaimsSet.class);

            mockedJWT.when(() -> SignedJWT.parse(token)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);
            when(claimsSet.getStringClaim("sub")).thenReturn(userId.toString());

            user.getBalance().setAmount(5);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            Operation additionOperation = new Operation(OperationType.ADDITION, 10);
            when(operationRepository.findByType(OperationType.ADDITION)).thenReturn(Optional.of(additionOperation));

            PaymentRequiredException exception = assertThrows(PaymentRequiredException.class,
                    () -> operationService.doOperation(token, type, value1, value2));
            assertEquals("Insufficient User Balance for this operation", exception.getMessage());
        }
    }

    @Test
    void testGetUserIdFromToken_ValidToken() throws ParseException {
        String token = "valid.token.here";
        UUID expectedUserId = UUID.randomUUID();

        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            SignedJWT signedJWT = mock(SignedJWT.class);
            JWTClaimsSet claimsSet = mock(JWTClaimsSet.class);

            mockedJWT.when(() -> SignedJWT.parse(token)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);
            when(claimsSet.getStringClaim("sub")).thenReturn(expectedUserId.toString());

            UUID actualUserId = operationService.getUserIdFromToken(token);

            assertEquals(expectedUserId, actualUserId);
        }
    }

    @Test
    void testGetUserIdFromToken_InvalidUUID() throws ParseException {
        String token = "valid.token.here";

        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            SignedJWT signedJWT = mock(SignedJWT.class);
            JWTClaimsSet claimsSet = mock(JWTClaimsSet.class);

            mockedJWT.when(() -> SignedJWT.parse(token)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);
            when(claimsSet.getStringClaim("sub")).thenReturn("invalid-uuid");

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> operationService.getUserIdFromToken(token));
            assertEquals("Token contains invalid user ID format: Invalid UUID format: invalid-uuid", exception.getMessage());
        }
    }

    @Test
    void testFindUserById_Success() {
        UUID userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User foundUser = operationService.findUserById(userId);

        assertEquals(user, foundUser);
        verify(userRepository).findById(userId);
    }

    @Test
    void testFindUserById_NotFound() {
        UUID userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> operationService.findUserById(userId));
        assertEquals("User not found. Try logging in again", exception.getMessage());
    }

    @Test
    void testCheckBalance_SufficientFunds() {
        user.getBalance().setAmount(50);
        when(userRepository.save(user)).thenReturn(user);

        User updatedUser = operationService.checkBalance(user, 20);

        assertEquals(30, updatedUser.getBalance().getAmount());
        verify(userRepository).save(user);
    }

    @Test
    void testCheckBalance_InsufficientFunds() {
        user.getBalance().setAmount(5);

        PaymentRequiredException exception = assertThrows(PaymentRequiredException.class,
                () -> operationService.checkBalance(user, 10));
        assertEquals("Insufficient User Balance for this operation", exception.getMessage());
    }

    @Test
    void testValidateOperationValues_Value2Missing() {
        OperationType type = OperationType.ADDITION;
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> operationService.validateOperationValues(type, value1, null));
        assertEquals("The second value (value2) is required for operation: ADDITION", exception.getMessage());
    }

    @Test
    void testGetOperation_NotFound() {
        when(operationRepository.findByType(OperationType.ADDITION)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> operationService.getOperation(OperationType.ADDITION, BigDecimal.TEN, BigDecimal.ONE));
        assertEquals("Operation type not found", exception.getMessage());
    }

    @Test
    void testDoOperation_InvalidFormatForValue1() throws ParseException {
        String token = "valid.token.here";
        UUID userId = UUID.randomUUID();
        String type = "ADDITION";

        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            SignedJWT signedJWT = mock(SignedJWT.class);
            JWTClaimsSet claimsSet = mock(JWTClaimsSet.class);

            mockedJWT.when(() -> SignedJWT.parse(token)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);
            when(claimsSet.getStringClaim("sub")).thenReturn(userId.toString());

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> operationService.doOperation(token, type, null, value2));
            assertEquals("The first value (value1) is required for operation: ADDITION", exception.getMessage());
        }
    }

    @Test
    void testDoOperation_InvalidFormatForValue2() throws ParseException {
        String token = "valid.token.here";
        UUID userId = UUID.randomUUID();
        String type = "ADDITION";

        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            SignedJWT signedJWT = mock(SignedJWT.class);
            JWTClaimsSet claimsSet = mock(JWTClaimsSet.class);

            mockedJWT.when(() -> SignedJWT.parse(token)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);
            when(claimsSet.getStringClaim("sub")).thenReturn(userId.toString());

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> operationService.doOperation(token, type, value1, null));
            assertEquals("The second value (value2) is required for operation: ADDITION", exception.getMessage());
        }
    }


    @Test
    void testValidateOperationValues_Value1Missing() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> operationService.validateOperationValues(OperationType.ADDITION, null, value2));
        assertEquals("The first value (value1) is required for operation: ADDITION", exception.getMessage());
    }

    @Test
    void testValidateOperationValues_Value2NotAllowed() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> operationService.validateOperationValues(OperationType.SQUARE_ROOT, value1, value2));
        assertEquals("Invalid payload: value2 is not allowed for operation: SQUARE_ROOT", exception.getMessage());
    }

    @Test
    void testExecuteOperation_Addition() {
        String result = operationService.executeOperation(OperationType.ADDITION, BigDecimal.TEN, BigDecimal.ONE);
        assertEquals("11", result);
    }

    @Test
    void testExecuteOperation_Subtraction() {
        String result = operationService.executeOperation(OperationType.SUBTRACTION, BigDecimal.TEN, BigDecimal.ONE);
        assertEquals("9", result);
    }

    @Test
    void testExecuteOperation_Multiplication() {
        String result = operationService.executeOperation(OperationType.MULTIPLICATION, BigDecimal.TEN, BigDecimal.ONE);
        assertEquals("10", result);
    }

    @Test
    void testExecuteOperation_Division() {
        String result = operationService.executeOperation(OperationType.DIVISION, BigDecimal.TEN, BigDecimal.ONE);
        assertEquals("10", result);
    }

    @Test
    void testExecuteOperation_DivisionByZero() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> operationService.executeOperation(OperationType.DIVISION, BigDecimal.TEN, BigDecimal.ZERO));
        assertEquals("Division by 0 is not possible", exception.getMessage());
    }

    @Test
    void testExecuteOperation_RandomString() {
        when(client.fetchRandomString(5)).thenReturn("random");
        String result = operationService.executeOperation(OperationType.RANDOM_STRING, new BigDecimal("5"), null);
        assertEquals("random", result);
    }

    @Test
    void testExecuteOperation_UnsupportedOperation() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> operationService.executeOperation(null, BigDecimal.ONE, BigDecimal.ONE));
        assertEquals("Unsupported operation type: null", exception.getMessage());
    }

    @Test
    void testSquareRoot_Success() {
        String result = operationService.executeOperation(OperationType.SQUARE_ROOT, new BigDecimal("4"), null);
        assertEquals("2", result);
    }

    @Test
    void testSquareRoot_NegativeValue() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> operationService.executeOperation(OperationType.SQUARE_ROOT, new BigDecimal("-9"), null));
        assertEquals("Negative numbers don't have square roots", exception.getMessage());
    }

    @Test
    void testFormatBigDecimal_Integer() {
        String result = OperationServiceImpl.formatBigDecimal(new BigDecimal("100"));
        assertEquals("100", result);
    }

    @Test
    void testFormatBigDecimal_Decimal() {
        String result = OperationServiceImpl.formatBigDecimal(new BigDecimal("100.5000"));
        assertEquals("100,5", result);
    }

    @Test
    void shouldThrowExceptionWhenOperationTypeIsNotSupported() {
        assertThrows(BadRequestException.class, () -> {
            operationService.validateOperationValues(null, value1, value2);
        });
    }

    @Test
    void shouldThrowExceptionWhenSecondValueIsMissing() {
        assertThrows(BadRequestException.class, () -> {
            operationService.validateOperationValues(OperationType.DIVISION, value1, null);
        });
        assertThrows(BadRequestException.class, () -> {
            operationService.validateOperationValues(OperationType.ADDITION, value1, null);
        });
        assertThrows(BadRequestException.class, () -> {
            operationService.validateOperationValues(OperationType.SUBTRACTION, value1, null);
        });
        assertThrows(BadRequestException.class, () -> {
            operationService.validateOperationValues(OperationType.MULTIPLICATION, value1, null);
        });
    }

    @Test
    void shouldThrowExceptionWhenUserBalanceIsInsufficient() {
        user.setBalance(new Balance(1));
        Integer cost = 10;

        assertThrows(PaymentRequiredException.class, () -> {
            operationService.checkBalance(user, cost);
        });
    }

    @Test
    void shouldThrowExceptionWhenResultIsNegative() {
        BigDecimal negative = BigDecimal.valueOf(-1);

        assertThrows(UnsupportedOperationException.class, () -> {
            operationService.executeOperation(OperationType.SQUARE_ROOT, negative, null);
        });
    }

    @Test
    void shouldThrowExceptionWhenThereAreTwoValuesForSingleValueOperation() {
        assertThrows(BadRequestException.class, () -> {
            operationService.validateOperationValues(OperationType.SQUARE_ROOT, value1, value2);
        });
        assertThrows(BadRequestException.class, () -> {
            operationService.validateOperationValues(OperationType.RANDOM_STRING, value1, value2);
        });
    }

    @Test
    void shouldThrowExceptionWhenOperationTypeIsInvalid() {
        assertThrows(BadRequestException.class, () -> {
            OperationServiceImpl.getOperationType("invalid type");
        });
    }

}