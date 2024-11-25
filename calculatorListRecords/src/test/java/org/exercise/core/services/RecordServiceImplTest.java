package org.exercise.core.services;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.exercise.core.entities.Record;
import org.exercise.core.entities.User;
import org.exercise.core.exceptions.BadRequestException;
import org.exercise.core.exceptions.NotFoundException;
import org.exercise.infrastructure.persistence.RecordRepository;
import org.exercise.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.text.ParseException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecordServiceImplTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecordServiceImpl recordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRecords_Success() throws Exception {
        String token = "validToken";
        UUID userId = UUID.randomUUID();
        User user = new User();
        Page<Record> mockPage = new PageImpl<>(Collections.singletonList(new Record()));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(recordRepository.findAllByUserAndDeletedFalseOrderByDateAsc(eq(user), any(Pageable.class))).thenReturn(mockPage);

        SignedJWT signedJWT = mock(SignedJWT.class);
        JWTClaimsSet jwtClaimsSet = mock(JWTClaimsSet.class);

        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            mockedJWT.when(() -> SignedJWT.parse(token)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenReturn(jwtClaimsSet);
            when(jwtClaimsSet.getStringClaim("sub")).thenReturn(userId.toString());

            Page<Record> result = recordService.getRecords(token, 0, 10, "");

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(userRepository, times(1)).findById(userId);
            verify(recordRepository, times(1)).findAllByUserAndDeletedFalseOrderByDateAsc(eq(user), any(Pageable.class));
        }
    }

    @Test
    void testGetRecords_UserNotFound() throws Exception {
        String token = "validToken";
        UUID userId = UUID.randomUUID();

        SignedJWT signedJWT = mock(SignedJWT.class);
        JWTClaimsSet jwtClaimsSet = mock(JWTClaimsSet.class);

        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            mockedJWT.when(() -> SignedJWT.parse(token)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenReturn(jwtClaimsSet);
            when(jwtClaimsSet.getStringClaim("sub")).thenReturn(userId.toString());

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                recordService.getRecords(token, 0, 10, "");
            });
            assertEquals("User was not found. Please try logging in again", exception.getMessage());
            verify(userRepository, times(1)).findById(userId);
        }
    }

    @Test
    void testGetUserIdFromToken_InvalidTokenFormat() {
        String invalidToken = "invalidToken";

        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            mockedJWT.when(() -> SignedJWT.parse(invalidToken)).thenThrow(new ParseException("Invalid token", 0));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                recordService.getRecords(invalidToken, 0, 10, "");
            });
            assertTrue(exception.getMessage().contains("Invalid ID token or user ID format"));
        }
    }

    @Test
    void testGetUserIdFromToken_MissingUserId() throws Exception {
        String token = "validToken";

        SignedJWT signedJWT = mock(SignedJWT.class);
        JWTClaimsSet jwtClaimsSet = mock(JWTClaimsSet.class);

        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            mockedJWT.when(() -> SignedJWT.parse(token)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenReturn(jwtClaimsSet);
            when(jwtClaimsSet.getStringClaim("sub")).thenReturn(null);

            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                recordService.getRecords(token, 0, 10, "");
            });
            assertTrue(exception.getMessage().contains("Token does not contain user ID"));
        }
    }

    @Test
    void testGetUserIdFromToken_InvalidUUIDFormat() throws Exception {
        String token = "validToken";
        String invalidUUID = "invalid-uuid-format";

        SignedJWT signedJWT = mock(SignedJWT.class);
        JWTClaimsSet jwtClaimsSet = mock(JWTClaimsSet.class);

        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            mockedJWT.when(() -> SignedJWT.parse(token)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenReturn(jwtClaimsSet);
            when(jwtClaimsSet.getStringClaim("sub")).thenReturn(invalidUUID);

            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                recordService.getRecords(token, 0, 10, "");
            });

            assertTrue(exception.getMessage().contains("Token contains invalid user ID format"));
            assertTrue(exception.getMessage().contains(invalidUUID));
        }
    }
}
