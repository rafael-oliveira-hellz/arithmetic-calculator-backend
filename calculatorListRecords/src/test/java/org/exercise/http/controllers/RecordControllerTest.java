package org.exercise.http.controllers;

import org.exercise.core.entities.Record;
import org.exercise.core.interfaces.RecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

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
    void testGetRecords() {
        String accessToken = "mockAccessToken";
        int page = 0;
        int size = 10;

        Record mockRecord = new Record();
        List<Record> recordList = Collections.singletonList(mockRecord);
        Page<Record> mockPage = new PageImpl<>(recordList);

        when(recordService.getRecords(accessToken, page, size, "")).thenReturn(mockPage);

        ResponseEntity<Page<Record>> response = recordController.getRecords(accessToken, page, size, "");

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(recordService, times(1)).getRecords(
                tokenCaptor.capture(),
                pageCaptor.capture(),
                sizeCaptor.capture(),
                eq("")
        );

        assertEquals(accessToken, tokenCaptor.getValue());
        assertEquals(page, pageCaptor.getValue());
        assertEquals(size, sizeCaptor.getValue());

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
    }
}
