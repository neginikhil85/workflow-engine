package dev.base.workflow.constant;

/**
 * Error message constants.
 */
public final class WorkflowErrorConstants {

    private WorkflowErrorConstants() {
        // Prevent instantiation
    }

    // --- General Errors ---
    public static final String ERR_WORKFLOW_NOT_FOUND = "Workflow not found: ";
    public static final String ERR_RUN_NOT_FOUND = "Run not found: ";
    public static final String ERR_EXECUTION_CANCELLED = "Workflow execution was cancelled";
    public static final String ERR_STOPPED_BY_USER = "Stopped by user";
    public static final String ERR_NO_EXECUTOR = "No executor registered for nodeType: ";

    // --- Validation Errors ---
    public static final String ERR_VALIDATION_NOT_MAP = "Validation failed: input is not a Map";
    public static final String ERR_VALIDATION_MISSING_FMT = "Validation failed: %s missing";

    // --- Kafka Errors ---
    public static final String ERR_KAFKA_TOPIC_REQUIRED = "Kafka topic is required";
    public static final String ERR_KAFKA_BOOTSTRAP_REQUIRED = "Bootstrap servers are required";
    public static final String ERR_KAFKA_CONFIG_REQUIRED = "Kafka configuration is required";
    public static final String ERR_KAFKA_TOPIC_PRODUCER = "Kafka topic is required for producer";
    public static final String ERR_KAFKA_TOPIC_CONSUMER = "Kafka topic is required for consumer";

    // --- Adapter Errors ---
    public static final String ERR_HTTP_URL_MISSING = "HTTP adapter requires 'url' in configuration";
    public static final String ERR_HTTP_URL_EMPTY = "HTTP adapter 'url' cannot be empty";
    public static final String ERR_TEAMS_WEBHOOK_MISSING = "Teams adapter requires 'webhookUrl' in configuration";
    public static final String ERR_SLACK_TOKEN_MISSING = "Slack adapter requires 'botToken' in configuration";
    public static final String ERR_SLACK_CHANNEL_MISSING = "Slack adapter requires 'channel' in configuration";
    public static final String ERR_WHATSAPP_APIKEY_MISSING = "WhatsApp adapter requires 'apiKey' in configuration";
    public static final String ERR_WHATSAPP_PHONE_EMPTY = "WhatsApp 'defaultPhoneNumber' cannot be empty";
    public static final String ERR_WHATSAPP_RECIPIENT_MISSING = "WhatsApp requires recipient phone number";
    public static final String ERR_EMAIL_TO_MISSING = "Email 'to' address is required";

}
