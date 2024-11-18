package org.exercise.infrastructure.persistence;

import org.exercise.core.entities.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OperationRepository extends JpaRepository<Operation, UUID> {
}
