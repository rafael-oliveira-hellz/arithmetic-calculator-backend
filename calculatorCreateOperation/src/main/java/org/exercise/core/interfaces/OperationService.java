package org.exercise.core.interfaces;

import org.exercise.core.entities.Operation;
import org.exercise.core.entities.Record;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public interface OperationService {

    Record doOperation(String token, String type, String value1Str, String value2Str);
    Operation getOperation(String type, BigDecimal value1, BigDecimal value2);
}
