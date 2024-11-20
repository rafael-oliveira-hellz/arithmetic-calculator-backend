package org.exercise.domain.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.exercise.domain.entities.Operation;
import org.exercise.domain.enums.OperationType;
import org.exercise.domain.repository.OperationRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InitDatabaseService {

    private final OperationRepository operationRepository;

    private final Operation op1 = new Operation(OperationType.ADDITION, 5);
    private final Operation op2 = new Operation(OperationType.SUBTRACTION, 5);
    private final Operation op3 = new Operation(OperationType.MULTIPLICATION, 5);
    private final Operation op4 = new Operation(OperationType.DIVISION, 5);
    private final Operation op5 = new Operation(OperationType.SQUARE_ROOT, 5);
    private final Operation op6 = new Operation(OperationType.RANDOM_STRING, 5);

    @PostConstruct
    public void initDb() {
        operationRepository.saveAll(List.of(op1, op2, op3, op4, op5, op6));
    }
}

