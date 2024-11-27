package org.exercise.infrastructure.lambda;


import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.exercise.Application;
import org.exercise.core.exceptions.InternalErrorException;
import org.exercise.infrastructure.lambda.StreamLambdaHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest
@ImportAutoConfiguration(exclude = {org.springframework.cloud.function.serverless.web.ServerlessAutoConfiguration.class})
public class StreamLambdaHandlerTest {

    private static StreamLambdaHandler handler;
    private static Context lambdaContext;
    private static MockedStatic<SpringBootLambdaContainerHandler> mockedHandler;

    @BeforeAll
    public static void setUp() {
        System.setProperty("spring.profiles.active", "test");
        handler = new StreamLambdaHandler();
        lambdaContext = new MockLambdaContext();
        mockedHandler = mockStatic(SpringBootLambdaContainerHandler.class);
    }

    @Test
    void invalidResource_streamRequest_responds404() {
        InputStream requestStream = new AwsProxyRequestBuilder("/pong", HttpMethod.GET)
                                            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                                            .buildStream();
        ByteArrayOutputStream responseStream = new ByteArrayOutputStream();

        handle(requestStream, responseStream);

        AwsProxyResponse response = readResponse(responseStream);
        assertNotNull(response);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode());
    }

    @Test
    void testStreamLambdaHandler_InitializationSuccess() {
        StreamLambdaHandler handler = new StreamLambdaHandler();
        assertNotNull(handler);
    }

    @Test
    void shouldThrowInternalErrorExceptionWhenHandlerInitializationFails() {
        mockedHandler.when(() -> SpringBootLambdaContainerHandler.getAwsProxyHandler(Application.class))
                .thenThrow(new ContainerInitializationException("Initialization failed", null));

        StreamLambdaHandler.resetHandler();

        Exception exception = assertThrows(InternalErrorException.class, StreamLambdaHandler::initializeHandler);
        assert exception.getMessage().contains("Could not initialize Spring Boot application");
    }

    private void handle(InputStream is, ByteArrayOutputStream os) {
        try {
            handler.handleRequest(is, os, lambdaContext);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private AwsProxyResponse readResponse(ByteArrayOutputStream responseStream) {
        try {
            return LambdaContainerHandler.getObjectMapper().readValue(responseStream.toByteArray(), AwsProxyResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Error while parsing response: " + e.getMessage());
        }
        return null;
    }
}
