package com.learning.workflow.executor.integration;

import com.learning.workflow.engine.NodeExecutor;
import com.learning.workflow.model.core.ExecutionContext;
import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.core.NodeExecutionResult;
import com.learning.workflow.model.nodetype.IntegrationNodeType;
import com.learning.workflow.model.nodetype.NodeType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * HTTP Call node executor using native Java HttpClient.
 */
@Component
public class HttpCallExecutor implements NodeExecutor {

    private final HttpClient httpClient;

    public HttpCallExecutor() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public NodeType getSupportedNodeType() {
        return IntegrationNodeType.HTTP_CALL;
    }

    @Override
    public NodeExecutionResult execute(NodeDefinition node, Object input, ExecutionContext ctx) {
        Map<String, Object> config = node.getConfig();
        if (config == null || !config.containsKey("url")) {
            throw new IllegalArgumentException("HTTP Call required configuration missing: url");
        }

        String url = (String) config.get("url");
        String method = (String) config.getOrDefault("method", "GET").toString().toUpperCase();
        String body = (String) config.getOrDefault("body", "");

        // Build Request
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(url));

        // Method & Body
        if ("POST".equals(method)) {
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
        } else if ("PUT".equals(method)) {
            requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body));
        } else if ("DELETE".equals(method)) {
            requestBuilder.DELETE();
        } else {
            requestBuilder.GET();
        }

        // Headers
        if (config.containsKey("headers") && config.get("headers") instanceof List) {
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> headerList = (List<Map<String, String>>) config.get("headers");
                for (Map<String, String> h : headerList) {
                    if (h.containsKey("key") && h.containsKey("value") && !h.get("key").isBlank()) {
                        requestBuilder.header(h.get("key"), h.get("value"));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing headers: " + e.getMessage());
            }
        }

        // Auto-add Content-Type: application/json if missing and body exists
        boolean hasContentType = node.getConfig() != null && node.getConfig().containsKey("headers") &&
                ((List<Map<String, String>>) node.getConfig().get("headers")).stream()
                        .anyMatch(h -> "Content-Type".equalsIgnoreCase(h.get("key")));

        if (!hasContentType && (body != null && !body.isBlank())) {
            requestBuilder.header("Content-Type", "application/json");
        }

        // Execute
        System.out.println("Executing HTTP " + method + " to " + url);
        try {
            HttpResponse<String> response = httpClient.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            return NodeExecutionResult.success(node.getId(), Map.of(
                    "status", response.statusCode(),
                    "response", response.body(),
                    "headers", response.headers().map(),
                    "method", method));

        } catch (Exception e) {
            return NodeExecutionResult.success(node.getId(), Map.of(
                    "status", 500,
                    "error", e.getMessage()));
        }
    }
}
