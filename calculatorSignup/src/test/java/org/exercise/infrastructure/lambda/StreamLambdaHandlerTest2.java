package org.exercise.infrastructure.lambda;

import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import org.exercise.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class StreamLambdaHandlerTest2 {

    private StreamLambdaHandler handler;
    private SpringBootLambdaContainerHandler mockContainerHandler;

    @BeforeEach
    void setUp() {
        mockContainerHandler = mock(SpringBootLambdaContainerHandler.class);
        try (MockedStatic<SpringBootLambdaContainerHandler> mockedStatic = Mockito.mockStatic(SpringBootLambdaContainerHandler.class)) {
            mockedStatic.when(() -> SpringBootLambdaContainerHandler.getAwsProxyHandler(Application.class))
                    .thenReturn(mockContainerHandler);
            handler = new StreamLambdaHandler();
        }
    }

    @Test
    void testHandleRequest_ThrowsException() throws Exception {
        // Arrange
        ByteArrayInputStream inputStream = new ByteArrayInputStream("{}".getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Context mockContext = mock(Context.class);
        doThrow(new RuntimeException("Test exception")).when(mockContainerHandler)
                .proxyStream(inputStream, outputStream, mockContext);

        assertThrows(RuntimeException.class, () -> handler.handleRequest(inputStream, outputStream, mockContext));
        verify(mockContainerHandler, times(1)).proxyStream(inputStream, outputStream, mockContext);
    }
}
