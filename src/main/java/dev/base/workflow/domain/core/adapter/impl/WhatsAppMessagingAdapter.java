package dev.base.workflow.domain.core.adapter.impl;

import dev.base.workflow.domain.core.adapter.MessagingAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;

import static dev.base.workflow.constant.WorkflowConstants.*;
import static dev.base.workflow.constant.WorkflowErrorConstants.ERR_WHATSAPP_APIKEY_MISSING;
import static dev.base.workflow.constant.WorkflowErrorConstants.ERR_WHATSAPP_PHONE_EMPTY;
import static dev.base.workflow.constant.WorkflowErrorConstants.ERR_WHATSAPP_RECIPIENT_MISSING;

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
        return PROVIDER_WHATSAPP;
    }

    @Override
    public String getName() {
        return NAME_WHATSAPP;
    }

    @Override
    public Object execute(Map<String, Object> config, Object data) {
        validateConfig(config);
        String message = data instanceof String ? (String) data : data.toString();
        String phoneNumber = (String) config.get(CFG_DEFAULT_PHONE_NUMBER);
        return sendMessage(config, message, phoneNumber);
    }

    @Override
    public void validateConfig(Map<String, Object> config) {
        if (config == null || !config.containsKey(CFG_API_KEY)) {
            throw new IllegalArgumentException(ERR_WHATSAPP_APIKEY_MISSING);
        }
        if (config.containsKey(CFG_DEFAULT_PHONE_NUMBER)) {
            String phone = (String) config.get(CFG_DEFAULT_PHONE_NUMBER);
            if (phone == null || phone.trim().isEmpty()) {
                throw new IllegalArgumentException(ERR_WHATSAPP_PHONE_EMPTY);
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
        // String apiKey = (String) config.get(CFG_API_KEY);
        String phoneNumber = recipient != null ? recipient : (String) config.get(CFG_DEFAULT_PHONE_NUMBER);

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException(ERR_WHATSAPP_RECIPIENT_MISSING);
        }

        // TODO: Implement actual WhatsApp API call
        // This would depend on your WhatsApp provider (Twilio, WhatsApp Business API,
        // etc.)

        return Map.of(
                KEY_SUCCESS, true,
                KEY_PHONE_NUMBER, phoneNumber,
                KEY_MESSAGE, "Message sent to WhatsApp (mock implementation)");
    }

    @Override
    public Object sendMessageWithAttachments(Map<String, Object> config, String message,
            String recipient, Map<String, Object> attachments) {
        validateConfig(config);
        // TODO: Implement WhatsApp message with media attachments
        return sendMessage(config, message, recipient);
    }
}
