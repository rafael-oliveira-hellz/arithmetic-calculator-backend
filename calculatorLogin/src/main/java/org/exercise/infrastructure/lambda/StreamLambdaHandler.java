package org.exercise.infrastructure.lambda;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.apache.commons.io.IOUtils;
import org.exercise.Application;
import org.exercise.core.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class StreamLambdaHandler implements RequestStreamHandler {
    private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
    private static final Logger logger = LoggerFactory.getLogger(StreamLambdaHandler.class);

    static {
        try {
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(Application.class);
        } catch (ContainerInitializationException e) {
            logger.error(e.getMessage());
            throw new InternalErrorException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        logger.info("Iniciando a requisição Lambda...");
        try {
            handler.proxyStream(inputStream, outputStream, context);
            logger.info("Requisição processada com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao processar a requisição: ", e);
            throw e;
        }
    }
}