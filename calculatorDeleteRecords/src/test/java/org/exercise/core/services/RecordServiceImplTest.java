package org.exercise.core.services;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.exercise.core.entities.Record;
import org.exercise.core.entities.User;
import org.exercise.core.exceptions.BadRequestException;
import org.exercise.core.exceptions.ForbiddenException;
import org.exercise.core.exceptions.NotFoundException;
import org.exercise.infrastructure.persistence.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;


class RecordServiceImplTest {

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private RecordServiceImpl recordService;

    private UUID recordId;
    private UUID userId;
    private Record recordObject;
    private String validToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        recordId = UUID.randomUUID();
        userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        recordObject = new Record();
        recordObject.setId(recordId);
        recordObject.setUser(user);
        recordObject.setDeleted(false);

        validToken = "valid.jwt.token";
        invalidToken = "invalid.token";
    }

    @Test
    void testDeleteRecord_Success() throws Exception {
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(recordObject));

        SignedJWT signedJWT = mock(SignedJWT.class);
        JWTClaimsSet jwtClaimsSet = mock(JWTClaimsSet.class);
        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            mockedJWT.when(() -> SignedJWT.parse(validToken)).thenReturn(signedJWT);

            when(signedJWT.getJWTClaimsSet()).thenReturn(jwtClaimsSet);
            when(jwtClaimsSet.getStringClaim("sub")).thenReturn(userId.toString());

            recordService.deleteRecord(validToken, recordId);

            ArgumentCaptor<Record> captor = ArgumentCaptor.forClass(Record.class);
            verify(recordRepository).save(captor.capture());
            Record savedRecord = captor.getValue();

            assertTrue(savedRecord.getDeleted());
            verify(recordRepository).findById(recordId);
            verify(recordRepository).save(recordObject);
        }
    }


    @Test
    void testDeleteRecord_RecordNotFound() {
        when(recordRepository.findById(recordId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> recordService.deleteRecord(validToken, recordId)
        );

        assertEquals("Record with ID #" + recordId + " not found", exception.getMessage());
        verify(recordRepository).findById(recordId);
        verify(recordRepository, never()).save(any());
    }

    @Test
    void testDeleteRecord_RecordAlreadyDeleted() {
        recordObject.setDeleted(true);
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(recordObject));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> recordService.deleteRecord(validToken, recordId)
        );

        assertEquals("Record is already marked as deleted", exception.getMessage());
        verify(recordRepository).findById(recordId);
        verify(recordRepository, never()).save(any());
    }

    @Test
    void testDeleteRecord_RecordNotOwnedByUser() throws Exception {
        UUID anotherUserId = UUID.randomUUID();
        recordObject.getUser().setId(anotherUserId);
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(recordObject));

        SignedJWT signedJWT = mock(SignedJWT.class);
        JWTClaimsSet jwtClaimsSet = mock(JWTClaimsSet.class);
        try (MockedStatic<SignedJWT> mockedJWT = mockStatic(SignedJWT.class)) {
            mockedJWT.when(() -> SignedJWT.parse(validToken)).thenReturn(signedJWT);

            when(signedJWT.getJWTClaimsSet()).thenReturn(jwtClaimsSet);
            when(jwtClaimsSet.getStringClaim("sub")).thenReturn(userId.toString());

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> recordService.deleteRecord(validToken, recordId));

            assertEquals(String.format("Unauthorized access: Record does not belong to the user. Token user ID: %s, Record owner ID: %s",
                            userId, anotherUserId), exception.getMessage());

            verify(recordRepository).findById(recordId);
            verify(recordRepository, never()).save(any());
        }
    }


    @Test
    void testDeleteRecord_InvalidToken() {
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(recordObject));

        assertThrows(
                BadRequestException.class,
                () -> recordService.deleteRecord(invalidToken, recordId)
        );

        verify(recordRepository).findById(recordId);
        verify(recordRepository, never()).save(any());
    }

    @Test
    void testGetUserIdFromToken_InvalidToken() {
        assertThrows(
                NotFoundException.class,
                () -> recordService.deleteRecord(invalidToken, recordId)
        );
    }
}
