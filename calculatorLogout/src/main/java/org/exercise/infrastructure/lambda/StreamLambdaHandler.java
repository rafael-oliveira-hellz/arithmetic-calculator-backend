package org.exercise.infrastructure.lambda;


import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.exercise.Application;
import org.exercise.core.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamLambdaHandler implements RequestStreamHandler {
    private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
    private static final Logger logger = LoggerFactory.getLogger(StreamLambdaHandler.class);

    static {
        try {
            logger.info("Loading Spring Boot application");
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(Application.class);
        } catch (ContainerInitializationException e) {
            logger.error(e.getMessage());
            throw new InternalErrorException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }
}