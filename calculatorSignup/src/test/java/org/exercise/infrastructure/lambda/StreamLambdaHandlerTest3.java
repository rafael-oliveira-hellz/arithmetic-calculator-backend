package org.exercise.infrastructure.lambda;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import org.exercise.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class StreamLambdaHandlerTest3 {

    @Test
    void testStaticInitialization_Failure() {
        try (MockedStatic<SpringBootLambdaContainerHandler> mockedStatic = Mockito.mockStatic(SpringBootLambdaContainerHandler.class)) {
            mockedStatic.when(() -> SpringBootLambdaContainerHandler.getAwsProxyHandler(Application.class))
                    .thenThrow(new ContainerInitializationException("Initialization error", null));

            ExceptionInInitializerError error = assertThrows(ExceptionInInitializerError.class, () -> {
                Class.forName("org.exercise.infrastructure.lambda.StreamLambdaHandler");
            });

            Throwable cause = error.getCause();
            assertNotNull(cause);
            assertEquals("Could not initialize Spring Boot applicationcom.amazonaws.serverless.exceptions.ContainerInitializationException: Initialization error",
                    cause.getMessage());
        }
    }
}
