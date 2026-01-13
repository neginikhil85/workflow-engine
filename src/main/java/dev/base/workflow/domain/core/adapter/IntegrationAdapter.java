package dev.base.workflow.domain.core.adapter;

import java.util.Map;

/**
 * Adapter pattern interface for integrating with external services.
 * Allows adding new integrations (Slack, Teams, WhatsApp, etc.) without
 * modifying core code.
 */
public interface IntegrationAdapter {

    /**
     * Unique identifier for this adapter
     */
    String getAdapterId();

    /**
     * Display name
     */
    String getName();

    /**
     * Execute the integration with given configuration and data
     * 
     * @param config Configuration map (API keys, endpoints, etc.)
     * @param data   Data to process/send
     * @return Response from the integration
     */
    Object execute(Map<String, Object> config, Object data);

    /**
     * Validate the configuration before execution
     * 
     * @param config Configuration to validate
     * @throws IllegalArgumentException if configuration is invalid
     */
    void validateConfig(Map<String, Object> config);

    /**
     * Test the connection with given configuration
     * 
     * @param config Configuration to test
     * @return true if connection successful
     */
    boolean testConnection(Map<String, Object> config);
}
