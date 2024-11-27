package org.exercise.infrastructure.clients;

import org.exercise.core.exceptions.BadGatewayException;
import org.exercise.core.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RandomStringClientTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private Logger mockLogger;

    @InjectMocks
    private RandomStringClient randomStringClient;

    @BeforeEach
    void setUpEnvironment() throws Exception {
        randomStringClient = new RandomStringClient();

        setPrivateField(randomStringClient, "baseUrl", "http://mock-api.com");
        setPrivateField(randomStringClient, "len", "8");
    }

    private void setPrivateField(Object targetObject, String fieldName, Object value) throws Exception {
        Field field = targetObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(targetObject, value);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void fetchRandomString_negativeNumber_throwsBadRequestException() {
        assertThrows(BadRequestException.class, () -> randomStringClient.fetchRandomString(-1));
    }

    @Test
    void fetchRandomString_exceedsLimit_throwsBadRequestException() {
        assertThrows(BadRequestException.class, () -> randomStringClient.fetchRandomString(10001));
    }

    @Test
    void fetchRandomString_validNumber_returnsString() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockResponse.body()).thenReturn("randomstring");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        try (MockedStatic<HttpClient> mockedHttpClientStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedHttpClientStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            String result = randomStringClient.fetchRandomString(5);

            assertEquals("randomstring", result);
        }
    }
    @Test
    void fetchRandomString_ioException_throwsBadGatewayException() throws Exception {
        RandomStringClient randomStringClient = new RandomStringClient();

        HttpClient mockHttpClient = mock(HttpClient.class);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("IO error"));

        try (MockedStatic<HttpClient> mockedHttpClient = Mockito.mockStatic(HttpClient.class)) {
            mockedHttpClient.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> randomStringClient.fetchRandomString(5));

            assertEquals("URI with undefined scheme", exception.getMessage());
        }
    }

    @Test
    void fetchRandomString_interruptedException_throwsBadGatewayException() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("Interrupted"));

        try (MockedStatic<HttpClient> mockedHttpClient = Mockito.mockStatic(HttpClient.class)) {
            mockedHttpClient.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            BadGatewayException exception = assertThrows(BadGatewayException.class, () -> randomStringClient.fetchRandomString(5));

            assertEquals("An error occurred when trying to access the random string api", exception.getMessage());
            assertTrue(Thread.currentThread().isInterrupted());
        }
    }

    @Test
    void fetchRandomString_missingEnvironmentVariables_throwsException() {
        System.clearProperty("RSCLIENT_BASE_URL");
        System.clearProperty("RSCLIENT_LEN");

        RandomStringClient randomStringClient = new RandomStringClient();

        assertThrows(IllegalArgumentException.class, () -> randomStringClient.fetchRandomString(5));

        System.setProperty("RSCLIENT_BASE_URL", "http://mock-api.com");
        System.setProperty("RSCLIENT_LEN", "8");
    }

    @Test
    void fetchRandomString_validNumber_correctUrlFormed() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockResponse.body()).thenReturn("randomstring");

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

        try (MockedStatic<HttpClient> mockedHttpClient = Mockito.mockStatic(HttpClient.class)) {
            mockedHttpClient.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            randomStringClient.fetchRandomString(5);

            verify(mockHttpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

            HttpRequest capturedRequest = requestCaptor.getValue();
            String expectedUrl = "http://mock-api.com?num=5&len=8&digits=off&upperalpha=on&loweralpha=on&unique=on&format=plain&rnd=new";

            assertEquals(expectedUrl, capturedRequest.uri().toString());
        }
    }

    @Test
    void testFetchRandomString_IOException() throws Exception {
        when(mockHttpClient.send(any(), any())).thenThrow(new IOException("Test IO Exception"));

        BadGatewayException exception = assertThrows(BadGatewayException.class, () ->
                randomStringClient.fetchRandomString(10.0));

        assertEquals("An error occurred when trying to access the random string api", exception.getMessage());
    }

    @Test
    void testFetchRandomString_NoException() throws Exception {
        String validBaseUrl = "http://mock-api.com"; // Use a valid URL

        double validNum = 5000; // Valid input within range
        String expectedResponse = "randomString"; // Mocked response body
        String validUrl = validBaseUrl + "?num=5000&len=8&digits=off&upperalpha=on&loweralpha=on&unique=on&format=plain&rnd=new";

        // Mock HttpClient and HttpResponse
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        HttpRequest expectedRequest = HttpRequest.newBuilder()
                .uri(URI.create(validUrl))
                .GET()
                .build();

        when(mockResponse.body()).thenReturn(expectedResponse);
        when(mockHttpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        // Use MockStatic to mock HttpClient.newHttpClient()
        try (MockedStatic<HttpClient> mockedStaticHttpClient = mockStatic(HttpClient.class)) {
            mockedStaticHttpClient.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            // Act
            String result = randomStringClient.fetchRandomString(validNum);

            // Assert
            assertEquals(expectedResponse, result);
            verify(mockHttpClient).send(eq(expectedRequest), any(HttpResponse.BodyHandler.class));
        }
    }


}
