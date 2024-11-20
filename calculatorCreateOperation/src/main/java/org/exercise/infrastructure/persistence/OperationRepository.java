package org.exercise.infrastructure.persistence;

import org.exercise.core.entities.Operation;
import org.exercise.core.enums.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperationRepository extends JpaRepository<Operation, UUID> {

    Optional<Operation> findByType(OperationType type);
}
