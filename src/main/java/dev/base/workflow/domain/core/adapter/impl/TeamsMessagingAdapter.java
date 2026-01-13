package dev.base.workflow.domain.core.adapter.impl;

import dev.base.workflow.domain.core.adapter.MessagingAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;

import static dev.base.workflow.constant.WorkflowConstants.*;
import static dev.base.workflow.constant.WorkflowErrorConstants.ERR_TEAMS_WEBHOOK_MISSING;

/**
 * Microsoft Teams integration adapter.
 * Implements MessagingAdapter for Teams messaging capabilities.
 */
@Component
public class TeamsMessagingAdapter implements MessagingAdapter {

    @Override
    public String getAdapterId() {
        return PROVIDER_TEAMS;
    }

    @Override
    public String getName() {
        return NAME_TEAMS;
    }

    @Override
    public Object execute(Map<String, Object> config, Object data) {
        validateConfig(config);
        String message = data instanceof String ? (String) data : data.toString();
        String webhookUrl = (String) config.get(CFG_WEBHOOK_URL);
        return sendMessage(config, message, webhookUrl);
    }

    @Override
    public void validateConfig(Map<String, Object> config) {
        if (config == null || !config.containsKey(CFG_WEBHOOK_URL)) {
            throw new IllegalArgumentException(ERR_TEAMS_WEBHOOK_MISSING);
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
        String webhookUrl = recipient != null ? recipient : (String) config.get(CFG_WEBHOOK_URL);

        // TODO: Implement actual Teams webhook call
        // Use RestTemplate or WebClient to POST to Teams webhook URL

        return Map.of(
                KEY_SUCCESS, true,
                KEY_WEBHOOK_URL, webhookUrl,
                KEY_MESSAGE, "Message sent to Teams (mock implementation)");
    }

    @Override
    public Object sendMessageWithAttachments(Map<String, Object> config, String message,
            String recipient, Map<String, Object> attachments) {
        validateConfig(config);
        // TODO: Implement Teams adaptive card with attachments
        return sendMessage(config, message, recipient);
    }
}
