package org.exercise.infrastructure.persistence;

import org.exercise.core.entities.Record;
import org.exercise.core.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RecordRepository extends JpaRepository<Record, UUID> {

    Page<Record> findAllByUserAndDeletedFalseOrderByDateAsc(User user, Pageable pageable);

    @Query("SELECT r FROM Record r " +
            "JOIN FETCH r.operation o " +
            "WHERE r.user.id = :userId " +
            "AND r.deleted = false " +
            "ORDER BY o.type ASC")
    Page<Record> findAllByUserAndDeletedFalseOrderByOperationTypeAsc(@Param("userId") UUID userId, Pageable pageable);
}
