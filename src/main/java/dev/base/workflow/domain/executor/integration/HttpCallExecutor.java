package dev.base.workflow.domain.executor.integration;

import dev.base.workflow.util.StringUtils;
import dev.base.workflow.domain.engine.NodeExecutor;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.model.nodetype.IntegrationNodeType;
import dev.base.workflow.model.nodetype.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static dev.base.workflow.constant.WorkflowConstants.*;

/**
 * HTTP Call node executor using Spring RestClient.
 * Provides fluent API, automatic JSON binding, and clean error handling.
 */
@Component
@Slf4j
public class HttpCallExecutor implements NodeExecutor {

    private final RestClient restClient;

    public HttpCallExecutor(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public NodeType getSupportedNodeType() {
        return IntegrationNodeType.HTTP_CALL;
    }

    @Override
    public void validate(NodeDefinition node) {
        Map<String, Object> config = node.getConfig();
        if (config == null || !config.containsKey(CFG_URL)) {
            throw new IllegalArgumentException(
                    StringUtils.concat("HTTP Call required configuration missing: ", CFG_URL));
        }
    }

    @Override
    public NodeExecutionResult execute(NodeDefinition node, Object input, ExecutionContext ctx) {
        validate(node);

        Map<String, Object> config = node.getConfig();
        String url = (String) config.get(CFG_URL);
        String method = config.getOrDefault(CFG_METHOD, "GET").toString().toUpperCase();
        String body = (String) config.getOrDefault(CFG_BODY, "");

        log.info("Executing HTTP {} to {}", method, url);

        try {
            String response = executeRequest(url, method, body, config);

            return NodeExecutionResult.success(node.getId(), Map.of(
                    KEY_STATUS, 200,
                    "response", response != null ? response : "",
                    CFG_METHOD, method));

        } catch (Exception e) {
            log.error("HTTP request failed: {}", e.getMessage());
            return NodeExecutionResult.success(node.getId(), Map.of(
                    KEY_STATUS, 500,
                    KEY_ERROR, e.getMessage(),
                    CFG_METHOD, method));
        }
    }

    private String executeRequest(String url, String method, String body, Map<String, Object> config) {
        return switch (method) {
            case "POST" -> restClient.post()
                    .uri(url)
                    .headers(h -> applyHeaders(h, config))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            case "PUT" -> restClient.put()
                    .uri(url)
                    .headers(h -> applyHeaders(h, config))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            case "DELETE" -> restClient.delete()
                    .uri(url)
                    .headers(h -> applyHeaders(h, config))
                    .retrieve()
                    .body(String.class);

            default -> restClient.get()
                    .uri(url)
                    .headers(h -> applyHeaders(h, config))
                    .retrieve()
                    .body(String.class);
        };
    }

    @SuppressWarnings("unchecked")
    private void applyHeaders(HttpHeaders headers, Map<String, Object> config) {
        if (config.containsKey(CFG_HEADERS) && config.get(CFG_HEADERS) instanceof List) {
            List<Map<String, String>> headerList = (List<Map<String, String>>) config.get(CFG_HEADERS);
            for (Map<String, String> h : headerList) {
                String key = h.get(CFG_KEY);
                String value = h.get(CFG_VALUE);
                if (key != null && !key.isBlank() && value != null) {
                    headers.add(key, value);
                }
            }
        }
    }
}
