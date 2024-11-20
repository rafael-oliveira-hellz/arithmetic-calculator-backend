package org.exercise.core.services;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.exercise.core.entities.Record;
import org.exercise.core.entities.User;
import org.exercise.core.exceptions.BadRequestException;
import org.exercise.core.exceptions.NotFoundException;
import org.exercise.core.interfaces.RecordService;
import org.exercise.infrastructure.persistence.RecordRepository;
import org.exercise.infrastructure.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private static final Logger logger = LoggerFactory.getLogger(RecordServiceImpl.class);

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    public Page<Record> getRecords(String token, Integer page, Integer size) {
        logger.info("Fetching user from access token");
        UUID userId = getUserIdFromToken(token);
        logger.info("User id found: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User was not found. Please try logging in again"));
        logger.info("User found. Retrieving user's records not deleted...");
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Record> records = recordRepository.findAllByUserAndDeletedFalse(user, pageable);
        logger.info("Records retrieved");
        return records;
    }

    private UUID getUserIdFromToken(String idToken) {
        try {
            logger.info("Retrieving user id from token");
            SignedJWT signedJWT = SignedJWT.parse(idToken);
            return Optional.ofNullable(signedJWT.getJWTClaimsSet().getStringClaim("sub"))
                    .map(sub -> {
                        try {
                            return UUID.fromString(sub);
                        } catch (IllegalArgumentException e) {
                            throw new BadRequestException("Token contains invalid user ID format: " + sub + ". " + e);
                        }
                    })
                    .orElseThrow(() -> new BadRequestException("Token does not contain user ID"));
        } catch (ParseException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ID token or user ID format", e);
        }
    }

}
