package org.exercise.core.interfaces;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.exercise.core.entities.Record;
import org.exercise.core.exceptions.ForbiddenException;
import org.exercise.core.exceptions.NotFoundException;
import org.exercise.infrastructure.persistence.RecordRepository;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Optional;
import java.util.UUID;

@Service
public interface RecordService {

    void deleteRecord(String token, UUID id);
}
