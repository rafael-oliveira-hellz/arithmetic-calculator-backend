package org.exercise.infrastructure.persistence;

import org.exercise.core.entities.Record;
import org.exercise.core.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecordRepository extends JpaRepository<Record, UUID> {

    Page<Record> findAllByUserAndDeletedFalse(User user, Pageable pageable);
}
