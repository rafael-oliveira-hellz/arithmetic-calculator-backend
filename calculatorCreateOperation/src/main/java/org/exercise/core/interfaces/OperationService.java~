package org.exercise.core.interfaces;

import org.exercise.core.entities.Operation;
import org.exercise.core.entities.Record;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface OperationService {

    @Transactional
    public Record doOperation(String token, String type, Integer value1, Integer value2);
    public Operation getOperation(String type, Integer value1, Integer value2);
}
