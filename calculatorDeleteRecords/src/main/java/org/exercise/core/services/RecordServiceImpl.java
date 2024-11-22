package org.exercise.core.services;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.exercise.core.entities.Record;
import org.exercise.core.exceptions.BadRequestException;
import org.exercise.core.exceptions.NotFoundException;
import org.exercise.core.interfaces.RecordService;
import org.exercise.infrastructure.persistence.RecordRepository;
import org.exercise.core.exceptions.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private static final Logger logger = LoggerFactory.getLogger(RecordServiceImpl.class);
    private final RecordRepository recordRepository;

    @Override
    public void deleteRecord(String token, UUID id) {
        logger.info("Attempting to delete record with ID: {}", id);

        Record record = findRecordById(id);

        logger.info("Checking if the record is already marked as deleted...");
        ensureRecordIsNotDeleted(record);

        logger.info("Verifying ownership of the record...");
        UUID userIdFromToken = getUserIdFromToken(token);
        validateRecordOwnership(record, userIdFromToken);

        logger.info("Marking the record as deleted...");
        markRecordAsDeleted(record);

        logger.info("Record with ID: {} successfully marked as deleted", id);
    }

    private Record findRecordById(UUID id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Record with ID: {} not found in the database", id);
                    return new NotFoundException("Record with ID #" + id + " not found");
                });
    }

    private void ensureRecordIsNotDeleted(Record record) {
        if (Boolean.TRUE.equals(record.getDeleted())) {
            logger.warn("Attempt to delete an already deleted record with ID: {}", record.getId());
            throw new BadRequestException("Record is already marked as deleted");
        }
    }

    private UUID getUserIdFromToken(String token) {
        try {
            logger.info("Parsing access token to retrieve user ID...");
            SignedJWT signedJWT = SignedJWT.parse(token);
            return Optional.ofNullable(signedJWT.getJWTClaimsSet().getStringClaim("sub"))
                    .map(UUID::fromString)
                    .orElseThrow(() -> {
                        logger.warn("Token does not contain a valid user ID");
                        return new BadRequestException("Token does not contain a valid user ID");
                    });
        } catch (ParseException | IllegalArgumentException e) {
            logger.error("Error parsing access token: {}", e.getMessage());
            throw new BadRequestException("Invalid access token format or user ID", e);
        }
    }

    private void validateRecordOwnership(Record record, UUID userIdFromToken) {
        UUID recordOwnerId = record.getUser().getId();

        if (!userIdFromToken.equals(recordOwnerId)) {
            logger.warn("Unauthorized access attempt: Token user ID: {}, Record owner ID: {}", userIdFromToken, recordOwnerId);
            throw new ForbiddenException(String.format(
                    "Unauthorized access: Record does not belong to the user. Token user ID: %s, Record owner ID: %s",
                    userIdFromToken, recordOwnerId
            ));
        }
    }

    private void markRecordAsDeleted(Record record) {
        record.setDeleted(true);
        recordRepository.save(record);
        logger.info("Record with ID: {} successfully marked as deleted", record.getId());
    }
}
