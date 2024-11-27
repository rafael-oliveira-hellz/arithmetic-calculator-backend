package org.exercise.core.interfaces;

import org.exercise.core.entities.Operation;
import org.exercise.core.entities.Record;
import org.exercise.core.enums.OperationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public interface OperationService {

    @Transactional
    Record doOperation(String token, String type, BigDecimal value1, BigDecimal value2);
    Operation getOperation(OperationType operationType, BigDecimal value1, BigDecimal value2);
}
