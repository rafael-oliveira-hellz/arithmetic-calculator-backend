package org.exercise.http.controllers;

import org.exercise.core.interfaces.RecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecordControllerTest {

    @Mock
    private RecordService recordService;

    @InjectMocks
    private RecordController recordController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckStatus() {
        ResponseEntity<String> response = recordController.checkStatus();

        assertNotNull(response);
        assertEquals("Status ok", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testRegisterUser() {
        String accessToken = "mockAccessToken";
        UUID recordId = UUID.randomUUID();

        ResponseEntity<RecordController.Response> response = recordController.registerUser(accessToken, recordId);

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(recordService).deleteRecord(tokenCaptor.capture(), idCaptor.capture());

        assertEquals(accessToken, tokenCaptor.getValue());
        assertEquals(recordId, idCaptor.getValue());

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals("Record with id #" + recordId + " deleted successfully!", response.getBody().message());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
