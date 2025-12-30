package com.learning.workflow.core.adapter.impl;

import com.learning.workflow.core.adapter.IntegrationAdapter;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.time.Duration;

/**
 * HTTP/REST API integration adapter.
 * Uses Native Java HttpClient.
 */
@Component
public class HttpIntegrationAdapter implements IntegrationAdapter {

    private final HttpClient httpClient;

    public HttpIntegrationAdapter() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public String getAdapterId() {
        return "http";
    }

    @Override
    public String getName() {
        return "HTTP/REST API";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object execute(Map<String, Object> config, Object data) {
        validateConfig(config);
        String url = (String) config.get("url");
        String method = (String) config.getOrDefault("method", "POST").toString().toUpperCase();

        // Build URL with query parameters
        Object queryParamsObj = config.get("queryParams");
        if (queryParamsObj instanceof Map) {
            Map<String, Object> queryParams = (Map<String, Object>) queryParamsObj;
            if (!queryParams.isEmpty()) {
                StringBuilder urlBuilder = new StringBuilder(url);
                boolean firstParam = !url.contains("?");
                for (Map.Entry<String, Object> param : queryParams.entrySet()) {
                    urlBuilder.append(firstParam ? "?" : "&");
                    urlBuilder.append(param.getKey()).append("=").append(param.getValue());
                    firstParam = false;
                }
                url = urlBuilder.toString();
            }
        }

        // Body
        Object requestBody = config.get("body");
        if (requestBody == null || (requestBody instanceof String && ((String) requestBody).trim().isEmpty())) {
            requestBody = data;
        }
        String bodyStr = requestBody != null ? requestBody.toString() : "";

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json");

            if ("POST".equals(method)) {
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(bodyStr));
            } else if ("PUT".equals(method)) {
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(bodyStr));
            } else if ("DELETE".equals(method)) {
                requestBuilder.DELETE();
            } else {
                requestBuilder.GET();
            }

            // Execute
            HttpResponse<String> response = httpClient.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (Exception e) {
            throw new RuntimeException("HTTP Request failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void validateConfig(Map<String, Object> config) {
        if (config == null || !config.containsKey("url")) {
            throw new IllegalArgumentException("HTTP adapter requires 'url' in configuration");
        }
        String url = (String) config.get("url");
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("HTTP adapter 'url' cannot be empty");
        }
    }

    @Override
    public boolean testConnection(Map<String, Object> config) {
        try {
            validateConfig(config);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
