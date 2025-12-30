package com.learning.workflow.core.adapter.impl;

import com.learning.workflow.core.adapter.MessagingAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * WhatsApp integration adapter.
 * Implements MessagingAdapter for WhatsApp messaging capabilities.
 * 
 * Note: Requires WhatsApp Business API or third-party service like Twilio, etc.
 */
@Component
public class WhatsAppMessagingAdapter implements MessagingAdapter {

    @Override
    public String getAdapterId() {
        return "whatsapp";
    }

    @Override
    public String getName() {
        return "WhatsApp";
    }

    @Override
    public Object execute(Map<String, Object> config, Object data) {
        validateConfig(config);
        String message = data instanceof String ? (String) data : data.toString();
        String phoneNumber = (String) config.get("defaultPhoneNumber");
        return sendMessage(config, message, phoneNumber);
    }

    @Override
    public void validateConfig(Map<String, Object> config) {
        if (config == null || !config.containsKey("apiKey")) {
            throw new IllegalArgumentException("WhatsApp adapter requires 'apiKey' in configuration");
        }
        if (config.containsKey("defaultPhoneNumber")) {
            String phone = (String) config.get("defaultPhoneNumber");
            if (phone == null || phone.trim().isEmpty()) {
                throw new IllegalArgumentException("WhatsApp 'defaultPhoneNumber' cannot be empty");
            }
        }
    }

    @Override
    public boolean testConnection(Map<String, Object> config) {
        try {
            validateConfig(config);
            // TODO: Test connection to WhatsApp API provider
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Object sendMessage(Map<String, Object> config, String message, String recipient) {
        validateConfig(config);
        // String apiKey = (String) config.get("apiKey");
        String phoneNumber = recipient != null ? recipient : (String) config.get("defaultPhoneNumber");

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("WhatsApp requires recipient phone number");
        }

        // TODO: Implement actual WhatsApp API call
        // This would depend on your WhatsApp provider (Twilio, WhatsApp Business API,
        // etc.)

        return Map.of(
                "success", true,
                "phoneNumber", phoneNumber,
                "message", "Message sent to WhatsApp (mock implementation)");
    }

    @Override
    public Object sendMessageWithAttachments(Map<String, Object> config, String message,
            String recipient, Map<String, Object> attachments) {
        validateConfig(config);
        // TODO: Implement WhatsApp message with media attachments
        return sendMessage(config, message, recipient);
    }
}
