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
import org.exercise.core.exceptions.UnsupportedOperationException;
import org.exercise.core.interfaces.OperationService;
import org.exercise.infrastructure.clients.RandomStringClient;
import org.exercise.infrastructure.persistence.OperationRepository;
import org.exercise.infrastructure.persistence.RecordRepository;
import org.exercise.infrastructure.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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
    public Record doOperation(String token, String type, String value1Str, String value2Str) {
        try {
            logger.info("Retrieving user ID from access token");
            UUID userId = getUserIdFromToken(token);

            User user = findUserById(userId);

            BigDecimal value1 = value1Str != null ? parseBigDecimal(value1Str, "value1") : null;
            BigDecimal value2 = value2Str != null ? parseBigDecimal(value2Str, "value2") : null;

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

    private BigDecimal parseBigDecimal(String valueStr, String valueName) {
        try {
            return new BigDecimal(valueStr);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid format for " + valueName + ": " + valueStr);
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

    public Operation getOperation(String type, BigDecimal value1, BigDecimal value2) {
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

    private void checkValues(BigDecimal value1, BigDecimal value2, OperationType operationType) {
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

    String executeOperation(OperationType type, BigDecimal value1, BigDecimal value2) {
        try {
            switch (type) {
                case ADDITION:
                    return formatBigDecimal(value1.add(value2));
                case SUBTRACTION:
                    return formatBigDecimal(value1.subtract(value2));
                case MULTIPLICATION:
                    return formatBigDecimal(value1.multiply(value2));
                case DIVISION:
                    return division(value1, value2);
                case SQUARE_ROOT:
                    return squareRoot(value1);
                case RANDOM_STRING:
                    return client.fetchRandomString(value1.intValue());
                default:
                    throw new UnsupportedOperationException("Unsupported operation type: " + type);
            }
        } catch (UnsupportedOperationException | ArithmeticException e) {
            logger.error("Error executing operation: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String division(BigDecimal value1, BigDecimal value2) {
        if (value2.compareTo(BigDecimal.ZERO) == 0) {
            throw new UnsupportedOperationException("Division by 0 is not possible");
        }
        BigDecimal result = value1.divide(value2, 15, RoundingMode.HALF_UP);
        return formatBigDecimal(result);
    }

    private String squareRoot(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new UnsupportedOperationException("Negative numbers don't have square roots");
        }
        BigDecimal result = sqrt(value, new MathContext(15));
        return formatBigDecimal(result);
    }

    private BigDecimal sqrt(BigDecimal value, MathContext mc) {
        BigDecimal x0 = BigDecimal.ZERO;
        BigDecimal x1 = new BigDecimal(Math.sqrt(value.doubleValue()));
        while (!x0.equals(x1)) {
            x0 = x1;
            x1 = value.divide(x0, mc);
            x1 = x1.add(x0);
            x1 = x1.divide(BigDecimal.valueOf(2), mc);
        }
        return x1;
    }

    private static String formatBigDecimal(BigDecimal result) {
        if (result.scale() <= 0 || result.stripTrailingZeros().scale() <= 0) {
            return result.setScale(0, RoundingMode.HALF_UP).toPlainString();
        } else {
            return result.stripTrailingZeros().toPlainString().replace('.', ',');
        }
    }

    Record saveRecord(Operation operation, User user, String result) {
        Record recordObject = new Record(operation, user, operation.getCost(), user.getBalance().getAmount(), result);
        logger.info("Persisting record for operation: {} and user: {}", operation.getType(), user.getId());

        return recordRepository.save(recordObject);
    }
}