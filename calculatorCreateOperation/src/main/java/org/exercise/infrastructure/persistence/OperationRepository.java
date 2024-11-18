package org.exercise.infrastructure.persistence;

import org.exercise.core.entities.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Integer> {

}
