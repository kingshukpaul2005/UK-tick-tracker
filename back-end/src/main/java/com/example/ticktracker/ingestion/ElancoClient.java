package com.example.ticktracker.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class ElancoClient {

    private static final Logger log = LoggerFactory.getLogger(ElancoClient.class);

    private final RestClient client;
    private final int maxAttempts;
    private final long retryWaitMs;

    public ElancoClient(@Value("${elanco.api.base-url}") String baseUrl,
                        @Value("${elanco.api.max-attempts}") int maxAttempts,
                        @Value("${elanco.api.retry-wait-ms}") long retryWaitMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(20000);
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.maxAttempts = maxAttempts;
        this.retryWaitMs = retryWaitMs;
    }

    public List<ElancoRecord> fetchAll() {
        RestClientException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                List<ElancoRecord> body = client.get()
                        .uri("/tick-sightings")
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<ElancoRecord>>() { });
                if (body == null) {
                    throw new ElancoApiException("Elanco API returned an empty body", null);
                }
                log.info("Fetched {} records from Elanco API (attempt {})", body.size(), attempt);
                return body;
            } catch (RestClientException e) {
                last = e;
                log.warn("Elanco API attempt {}/{} failed: {}", attempt, maxAttempts, e.getMessage());
                if (attempt < maxAttempts) {
                    pause(retryWaitMs * attempt);
                }
            }
        }
        throw new ElancoApiException("Elanco API unavailable after " + maxAttempts + " attempts", last);
    }

    private void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ElancoApiException("Interrupted while waiting to retry", e);
        }
    }
}