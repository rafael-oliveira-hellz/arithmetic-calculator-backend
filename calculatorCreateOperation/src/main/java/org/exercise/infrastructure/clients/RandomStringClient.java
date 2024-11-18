package org.exercise.infrastructure.clients;

import lombok.RequiredArgsConstructor;
import org.exercise.core.exceptions.BadGatewayException;
import org.exercise.core.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@RequiredArgsConstructor
public class RandomStringClient {

    private static final Logger logger = LoggerFactory.getLogger(RandomStringClient.class);
    private final String baseUrl = System.getenv("RSCLIENT_BASE_URL");
    private final String len = System.getenv("RSCLIENT_LEN");

    public String fetchRandomString(Integer num) {
        String url = String.format("%s?num=%d&len=%s&digits=off&upperalpha=on&loweralpha=on&unique=on&format=plain&rnd=new",
                baseUrl, num, len);
        if (num < 0 || num > 10000) {
            throw new BadRequestException("Numeric value for Random String request cannot be negative nor greater than 10000");
        }
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            } catch (IOException e) {
                throw new BadGatewayException("An error occurred when trying to access the random string api");
            } catch (InterruptedException ie) {
                logger.error("InterruptedException: ", ie);
                Thread.currentThread().interrupt();
                throw new BadGatewayException("An error occurred when trying to access the random string api");
            }
        }
    }
}
