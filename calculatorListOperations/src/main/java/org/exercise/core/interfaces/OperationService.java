package org.exercise.core.interfaces;

import org.exercise.core.entities.Operation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OperationService {

    public List<Operation> getOperations();

}
