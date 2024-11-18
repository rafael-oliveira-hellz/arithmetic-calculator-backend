package org.exercise.core.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.exercise.core.entities.Operation;
import org.exercise.core.enums.OperationType;
import org.exercise.infrastructure.persistence.OperationRepository;

import java.util.List;

@RequiredArgsConstructor
public class InitDatabase {

    private final OperationRepository operationRepository;

    private final Operation op1 = new Operation(0, OperationType.ADDITION, Integer.parseInt(System.getenv("ADD_COST")));
    private final Operation op2 = new Operation(1, OperationType.SUBTRACTION, Integer.parseInt(System.getenv("SUB_COST")));
    private final Operation op3 = new Operation(2, OperationType.MULTIPLICATION, Integer.parseInt(System.getenv("MLT_COST")));
    private final Operation op4 = new Operation(3, OperationType.DIVISION, Integer.parseInt(System.getenv("DIV_COST")));
    private final Operation op5 = new Operation(4, OperationType.SQUARE_ROOT, Integer.parseInt(System.getenv("SQR_COST")));
    private final Operation op6 = new Operation(5, OperationType.RANDOM_STRING, Integer.parseInt(System.getenv("RDM_STR_COST")));

    private final List<Operation> operations = List.of(op1, op2, op3, op4, op5, op6);

    public void initTableIfEmptyOrCostsChanged() {
        if (operationRepository.count() == 0 || !operationRepository.findAll().equals(operations) ) {
            operationRepository.saveAll(operations);
        }
    }
}

