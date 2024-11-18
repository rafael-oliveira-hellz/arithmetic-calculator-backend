package org.exercise.core.services;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.exercise.core.entities.Record;
import org.exercise.core.entities.User;
import org.exercise.core.exceptions.NotFoundException;
import org.exercise.core.interfaces.RecordService;
import org.exercise.infrastructure.persistence.RecordRepository;
import org.exercise.infrastructure.persistence.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    public Page<Record> getRecords(String token, Integer page, Integer size) {
        String userId = getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User was not found. Please try logging in again"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return recordRepository.findAllByUserAndDeletedFalse(user, pageable);
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
