package com.learning.workflow.core.adapter.impl;

import com.learning.workflow.core.adapter.MessagingAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Microsoft Teams integration adapter.
 * Implements MessagingAdapter for Teams messaging capabilities.
 */
@Component
public class TeamsMessagingAdapter implements MessagingAdapter {
    
    @Override
    public String getAdapterId() {
        return "teams";
    }
    
    @Override
    public String getName() {
        return "Microsoft Teams";
    }
    
    @Override
    public Object execute(Map<String, Object> config, Object data) {
        validateConfig(config);
        String message = data instanceof String ? (String) data : data.toString();
        String webhookUrl = (String) config.get("webhookUrl");
        return sendMessage(config, message, webhookUrl);
    }
    
    @Override
    public void validateConfig(Map<String, Object> config) {
        if (config == null || !config.containsKey("webhookUrl")) {
            throw new IllegalArgumentException("Teams adapter requires 'webhookUrl' in configuration");
        }
    }
    
    @Override
    public boolean testConnection(Map<String, Object> config) {
        try {
            validateConfig(config);
            // TODO: Send test message to Teams webhook
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public Object sendMessage(Map<String, Object> config, String message, String recipient) {
        validateConfig(config);
        String webhookUrl = recipient != null ? recipient : (String) config.get("webhookUrl");
        
        // TODO: Implement actual Teams webhook call
        // Use RestTemplate or WebClient to POST to Teams webhook URL
        
        return Map.of(
            "success", true,
            "webhookUrl", webhookUrl,
            "message", "Message sent to Teams (mock implementation)"
        );
    }
    
    @Override
    public Object sendMessageWithAttachments(Map<String, Object> config, String message, 
                                            String recipient, Map<String, Object> attachments) {
        validateConfig(config);
        // TODO: Implement Teams adaptive card with attachments
        return sendMessage(config, message, recipient);
    }
}

