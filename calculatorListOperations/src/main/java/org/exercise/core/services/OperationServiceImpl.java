package org.exercise.core.services;

import lombok.RequiredArgsConstructor;
import org.exercise.core.entities.Operation;
import org.exercise.core.interfaces.OperationService;
import org.exercise.infrastructure.persistence.OperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {

    private static final Logger logger = LoggerFactory.getLogger(OperationServiceImpl.class);
    private final OperationRepository operationRepository;

    @Override
    public List<Operation> getOperations() {
        logger.info("Retrieving operation list...");
        List<Operation> operations = operationRepository.findAll();
        logger.info("List returned successfully");
        return operations;
    }
}
