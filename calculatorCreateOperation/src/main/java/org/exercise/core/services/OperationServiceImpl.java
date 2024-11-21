package org.exercise.core.services;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.exercise.core.entities.Operation;
import org.exercise.core.entities.Record;
import org.exercise.core.entities.User;
import org.exercise.core.enums.OperationType;
import org.exercise.core.exceptions.BadRequestException;
import org.exercise.core.exceptions.NotFoundException;
import org.exercise.core.exceptions.PaymentRequiredException;
import org.exercise.core.interfaces.OperationService;
import org.exercise.infrastructure.persistence.OperationRepository;
import org.exercise.infrastructure.persistence.RecordRepository;
import org.exercise.infrastructure.persistence.UserRepository;
import org.exercise.core.exceptions.UnsupportedOperationException;
import org.exercise.infrastructure.clients.RandomStringClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {

    private static final Logger logger = LoggerFactory.getLogger(OperationServiceImpl.class);

    private final UserRepository userRepository;
    private final OperationRepository operationRepository;
    private final RecordRepository recordRepository;
    private final RandomStringClient client;

    @Transactional
    public Record doOperation(String token, String type, Integer value1, Integer value2) {
        logger.info("Retrieving user id from access token");
        UUID userId = getUserIdFromToken(token);
        User user = findUserById(userId);
        Operation operation = getOperation(type, value1, value2);
        User updatedUser = checkBalance(user, operation.getCost());
        String result = executeOperation(operation.getType(), value1, value2);
        return saveRecord(operation, updatedUser, result);
    }

    private UUID getUserIdFromToken(String accessToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(accessToken);
            return Optional.ofNullable(signedJWT.getJWTClaimsSet().getStringClaim("sub"))
                    .map(sub -> {
                        try {
                            return UUID.fromString(sub);
                        } catch (IllegalArgumentException e) {
                            throw new BadRequestException("Token contains invalid user ID format: " + sub, e.getMessage());
                        }
                    })
                    .orElseThrow(() -> new BadRequestException("Token does not contain user ID"));
        } catch (ParseException e) {
            throw new BadRequestException("Invalid access token format", e.getMessage());
        }
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User not found. Try logging in again"));
    }

    public Operation getOperation(String type, Integer value1, Integer value2) {
        try {
            OperationType operationType = OperationType.valueOf(type.toUpperCase());
            checkValues(value1, value2, operationType);
            return operationRepository.findByType(operationType)
                    .orElseThrow(() -> new NotFoundException("Operation type not found"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid operation type: " + type, e);
        }
    }

    private void checkValues(Integer value1, Integer value2, OperationType operationType) {
        boolean needTwoValues = operationType.getId() >= 1 && operationType.getId() < 4;
        if (value1 == null) {
            throw new BadRequestException("The first value is required for operation: " + operationType);
        }
        if (needTwoValues && value2 == null) {
            throw new BadRequestException("The second value is required for operation: " + operationType);
        }
    }

    User checkBalance(User user, Integer cost) {
        int result = user.getBalance().getAmount() - cost;
        if (result < 0) {
            throw new PaymentRequiredException("Insufficient User Balance for this operation");
        }
        user.getBalance().setAmount(result);
        logger.info("User balance updated: {} for user: {}", result, user.getId());
        return userRepository.save(user);
    }

    String executeOperation(OperationType type, Integer value1, Integer value2) {
        return String.valueOf(switch (type) {
            case ADDITION -> safeAdd(value1, value2);
            case SUBTRACTION -> safeSubtract(value1, value2);
            case MULTIPLICATION -> safeMultiply(value1, value2);
            case DIVISION -> division(value1, value2);
            case SQUARE_ROOT -> squareRoot(value1);
            case RANDOM_STRING -> client.fetchRandomString(value1);
        });
    }

    private Integer safeAdd(Integer a, Integer b) {
        try {
            return Math.addExact(a, b);
        } catch (ArithmeticException e) {
            throw new UnsupportedOperationException("Integer overflow during addition", e.getMessage());
        }
    }

    private Integer safeSubtract(Integer a, Integer b) {
        try {
            return Math.subtractExact(a, b);
        } catch (ArithmeticException e) {
            throw new UnsupportedOperationException("Integer underflow during subtraction", e.getMessage());
        }
    }

    private Integer safeMultiply(Integer a, Integer b) {
        try {
            return Math.multiplyExact(a, b);
        } catch (ArithmeticException e) {
            throw new UnsupportedOperationException("Integer overflow during multiplication", e.getMessage());
        }
    }

    private String division(Integer value1, Integer value2) {
        if (value2 == 0) {
            throw new UnsupportedOperationException("Division by 0 is not possible");
        }

        double result = (double) value1 / value2;
        return (result % 1 == 0)
                ? String.valueOf((int) result)
                : String.format("%.2f", result).replace('.', ',');
    }

    private Integer squareRoot(Integer value1) {
        if (value1 < 0) {
            throw new UnsupportedOperationException("Negative numbers don't have square roots");
        }
        return (int) Math.sqrt(value1.doubleValue());
    }

    Record saveRecord(Operation operation, User user, String result) {
        Record record = new Record(operation, user, operation.getCost(), user.getBalance().getAmount(), result);
        logger.info("Persisting record for operation: {} and user: {}", operation.getType(), user.getId());
        return recordRepository.save(record);
    }
}