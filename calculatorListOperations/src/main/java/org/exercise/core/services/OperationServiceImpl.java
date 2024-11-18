package org.exercise.core.services;

import lombok.RequiredArgsConstructor;
import org.exercise.core.entities.Operation;
import org.exercise.core.interfaces.OperationService;
import org.exercise.infrastructure.persistence.OperationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {

    private final OperationRepository operationRepository;

    @Override
    public List<Operation> getOperations() {
        return operationRepository.findAll();
    }
}
