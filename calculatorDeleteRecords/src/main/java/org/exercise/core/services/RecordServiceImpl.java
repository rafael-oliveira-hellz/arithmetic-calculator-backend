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

    public void deleteRecord(String token, UUID id) {
        logger.info("Retrieving record with id #{} from database", id);
        Record recordObject = recordRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Record with id #" + id + " not found"));

        logger.info("Asserting isn't already marked as deleted...");
        assertRecordIsNotYetDeleted(recordObject);
        logger.info("Record is not deleted. Continuing...");

        logger.info("Record found. Asserting it belongs to user");
        UUID userIdFromToken = UUID.fromString(getUserIdFromToken(token));
        UUID recordOwnerId = recordObject.getUser().getId();

        if (!userIdFromToken.equals(recordOwnerId)) {
            throw new ForbiddenException(String.format(
                    "Record does not belong to the user. Token user ID: %s, Record owner ID: %s",
                    userIdFromToken,
                    recordOwnerId
            ));
        }
        
        logger.info("Record belongs to user. Marking as deleted...");
        recordObject.setDeleted(true);
        recordRepository.save(recordObject);
        logger.info("Marking as deleted successful");
    }

    private static void assertRecordIsNotYetDeleted(Record recordObject) {
        if(Boolean.TRUE.equals(recordObject.getDeleted())) {
            throw new BadRequestException("Record is already marked as deleted");
        }
    }

    private String getUserIdFromToken(String idToken) {
        try {
            logger.info("Retrieving id from access token...");
            SignedJWT signedJWT = SignedJWT.parse(idToken);
            String id = Optional.ofNullable(signedJWT.getJWTClaimsSet().getStringClaim("sub"))
                    .orElseThrow(() -> new IllegalArgumentException("Token does not contain user ID"));
            logger.info("User id #{} retrieved successfully", id);
            return id;
        } catch (ParseException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ID token or user ID format", e);
        }
    }
}
