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
    public Record doOperation(String token, String type, Double value1, Double value2) {
        try {
            logger.info("Retrieving user ID from access token");
            UUID userId = getUserIdFromToken(token);

            User user = findUserById(userId);
            Operation operation = getOperation(type, value1, value2);

            User updatedUser = checkBalance(user, operation.getCost());
            String result = executeOperation(operation.getType(), value1, value2);

            return saveRecord(operation, updatedUser, result);
        } catch (UnsupportedOperationException e) {
            logger.error("Operation not supported: {}", e.getMessage(), e);
            throw e;
        } catch (BadRequestException e) {
            logger.warn("Bad request: {}", e.getMessage(), e);
            throw e;
        } catch (PaymentRequiredException e) {
            logger.error("Payment required: {}", e.getMessage(), e);
            throw e;
        } catch (NotFoundException e) {
            logger.warn("Resource not found: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred. Please try again later.");
        }
    }

    private UUID getUserIdFromToken(String accessToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(accessToken);
            return Optional.ofNullable(signedJWT.getJWTClaimsSet().getStringClaim("sub"))
                    .map(sub -> {
                        try {
                            return UUID.fromString(sub);
                        } catch (IllegalArgumentException e) {
                            throw new BadRequestException(
                                    "Token contains invalid user ID format",
                                    "Invalid UUID format: " + sub
                            );
                        }
                    })
                    .orElseThrow(() -> new BadRequestException("Token does not contain user ID"));
        } catch (ParseException e) {
            throw new BadRequestException("Invalid access token format", e.getMessage());
        }
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            logger.warn("User not found with ID: {}", userId);
            return new NotFoundException("User not found. Try logging in again");
        });
    }

    public Operation getOperation(String type, Double value1, Double value2) {
        try {
            OperationType operationType = OperationType.valueOf(type.toUpperCase());
            checkValues(value1, value2, operationType);

            return operationRepository.findByType(operationType)
                    .orElseThrow(() -> {
                        logger.warn("Operation type not found: {}", operationType);
                        return new NotFoundException("Operation type not found");
                    });
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid operation type: " + type, e.getMessage());
        }
    }

    private void checkValues(Double value1, Double value2, OperationType operationType) {
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

    String executeOperation(OperationType type, Double value1, Double value2) {
        try {
            return switch (type) {
                case ADDITION -> formatDouble(value1 + value2);
                case SUBTRACTION -> formatDouble(value1 - value2);
                case MULTIPLICATION -> formatDouble(value1 * value2);
                case DIVISION -> division(value1, value2);
                case SQUARE_ROOT -> squareRoot(value1);
                case RANDOM_STRING -> client.fetchRandomString(value1);
            };
        } catch (UnsupportedOperationException | ArithmeticException e) {
            logger.error("Error executing operation: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String division(Double value1, Double value2) {
        if (value2 == 0) {
            throw new UnsupportedOperationException("Division by 0 is not possible");
        }
        return formatDouble(value1 / value2);
    }

    private String squareRoot(Double value1) {
        if (value1 < 0) {
            throw new UnsupportedOperationException("Negative numbers don't have square roots");
        }
        return formatDouble(Math.sqrt(value1));
    }

    private static String formatDouble(double result) {
        return (result % 1 == 0)
                ? String.valueOf((int) result)
                : String.format("%.2f", result).replace('.', ',');
    }

    Record saveRecord(Operation operation, User user, String result) {
        Record recordObject = new Record(operation, user, operation.getCost(), user.getBalance().getAmount(), result);
        logger.info("Persisting record for operation: {} and user: {}", operation.getType(), user.getId());

        return recordRepository.save(recordObject);
    }
}