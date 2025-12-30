package com.learning.workflow.core.adapter;

import java.util.Map;

/**
 * Specialized adapter for messaging platforms (Slack, Teams, WhatsApp, Email, etc.)
 */
public interface MessagingAdapter extends IntegrationAdapter {
    
    /**
     * Send a message
     * 
     * @param config Configuration (channel, API keys, etc.)
     * @param message Message content
     * @param recipient Recipient information
     * @return Response from messaging platform
     */
    Object sendMessage(Map<String, Object> config, String message, String recipient);
    
    /**
     * Send a message with attachments
     * 
     * @param config Configuration
     * @param message Message content
     * @param recipient Recipient
     * @param attachments Attachment data
     * @return Response from messaging platform
     */
    Object sendMessageWithAttachments(Map<String, Object> config, String message, 
                                     String recipient, Map<String, Object> attachments);
}

