package org.exercise.core.services;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.exercise.core.entities.Record;
import org.exercise.core.exceptions.NotFoundException;
import org.exercise.core.interfaces.RecordService;
import org.exercise.infrastructure.persistence.RecordRepository;
import org.exercise.core.exceptions.ForbiddenException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final RecordRepository recordRepository;

    public void deleteRecord(String token, UUID id) {
        Record recordObject = recordRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Record with id #" + id + " not found"));
        if (!getUserIdFromToken(token).equals(recordObject.getUser().getId()))
            throw new ForbiddenException("Record does not belong to this user, and thus cannot be modified");
        recordObject.setDeleted(true);
        recordRepository.save(recordObject);
    }

    private String getUserIdFromToken(String idToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(idToken);
            return Optional.ofNullable(signedJWT.getJWTClaimsSet().getStringClaim("sub"))
                    .orElseThrow(() -> new IllegalArgumentException("Token does not contain user ID"));
        } catch (ParseException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ID token or user ID format", e);
        }
    }
}
