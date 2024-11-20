package org.exercise.infrastructure.lambda;

import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import org.exercise.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
class StreamLambdaHandlerTest {

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

        System.setProperty("jdk.instrument.traceUsage", "false");
        System.setProperty("jdk.bytebuddy.suppress", "true");
    }

    @Test
    void testHandleRequest_SuccessfulInvocation() throws IOException {
        // Arrange
        ByteArrayInputStream inputStream = new ByteArrayInputStream("{}".getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Context mockContext = mock(Context.class);

        // Act
        handler.handleRequest(inputStream, outputStream, mockContext);

        // Assert
        verify(mockContainerHandler, times(1)).proxyStream(inputStream, outputStream, mockContext);
    }


}
