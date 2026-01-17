package dev.base.workflow.model.node.details;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class HttpExecutionDetails {
    private int status;
    private String method;
    private String url;

    private Object requestBody;
    private Map<String, String> requestHeaders;

    private Map<String, String> headers; // Response Headers
    private Object response; // Response Body
    private String error;
}
