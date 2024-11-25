package org.exercise.core.interfaces;

import org.exercise.core.entities.Operation;
import org.exercise.core.entities.Record;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface OperationService {

    Record doOperation(String token, String type, Double value1, Double value2);
    Operation getOperation(String type, Double value1, Double value2);
}
