package org.exercise.core.services;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.exercise.core.config.InitDatabase;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {

    private final UserRepository userRepository;
    private final OperationRepository operationRepository;
    private final RecordRepository recordRepository;
    private final RandomStringClient client;
    private final InitDatabase initDatabase;

    @Transactional
    public Record doOperation(String token, String type, Integer value1, Integer value2) {
        String userId = getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User not found. Try logging in again"));
        Operation operation = getOperation(type, value1, value2);
        User updatedUser = checkBalance(user, operation.getCost());
        String result = (String.valueOf( switch (operation.getType()) {
            case ADDITION -> value1 + value2;
            case SUBTRACTION -> value1 - value2;
            case MULTIPLICATION -> value1 * value2;
            case DIVISION -> division(value1, value2);
            case SQUARE_ROOT -> squareRoot(value1);
            case RANDOM_STRING -> client.fetchRandomString(value1);
        }));
        return recordRepository.save(
                new Record(operation, user, operation.getCost(), updatedUser.getBalance().getAmount(), result));
    }

    private String getUserIdFromToken(String idToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(idToken);
            return Optional.ofNullable(signedJWT.getJWTClaimsSet().getStringClaim("sub"))
                    .orElseThrow(() -> new IllegalArgumentException("Token does not contain user ID"));
        } catch (ParseException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ID token or user ID format", e);
        }
    }

    public Operation getOperation(String type, Integer value1, Integer value2) {
        try {
            OperationType operationType = OperationType.valueOf(type.toUpperCase());
            checkValues(value1, value2, operationType);
            initDatabase.initTableIfEmptyOrCostsChanged();
            return operationRepository.findById(operationType.getId())
                    .orElseThrow(() -> new NotFoundException("Operation type not found"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid operation type: " + type);
        }
    }

    private static void checkValues(Integer value1, Integer value2, OperationType operationType) {
        boolean needTwoValues = operationType.getId() >= 1 && operationType.getId() < 4;
        if (value1 == null || (needTwoValues && value2 == null)) {
            throw new BadRequestException("Please insert one or both values, according to the operation type");
        }
    }

    private Integer division(Integer value1, Integer value2) {
        if (value2 == 0) throw new UnsupportedOperationException("Division by 0 is not possible");
        return value1 / value2;
    }

    private Integer squareRoot(Integer value1) {
        if (value1 < 0) throw new UnsupportedOperationException("Negative numbers don't have square roots");
        return (int) Math.sqrt(value1.doubleValue());
    }

    private User checkBalance(User user, Integer cost) {
        int result = user.getBalance().getAmount() - cost;
        if (result < 0) throw new PaymentRequiredException("Insufficient User Balance for this operation");
        user.getBalance().setAmount(result);
        return userRepository.save(user);
    }

}
