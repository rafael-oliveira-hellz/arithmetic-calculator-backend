package org.exercise.infrastructure.persistence;

import org.exercise.core.entities.Record;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecordRepository extends JpaRepository<Record, UUID> {

}
