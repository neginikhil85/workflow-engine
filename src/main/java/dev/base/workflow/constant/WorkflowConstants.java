package dev.base.workflow.constant;

/**
 * Application-wide constants.
 * Centralizes error messages, log prefixes, and magic numbers.
 */
public final class WorkflowConstants {

    private WorkflowConstants() {
        // Prevent instantiation
    }

    // --- Log Prefixes ---
    public static final String LOG_STARTING_EXECUTION = ">>> STARTING WORKFLOW EXECUTION: {}";
    public static final String LOG_STOPPING_WORKFLOW = ">>> STOPPING WORKFLOW: {}";
    public static final String LOG_WORKFLOW_STOPPED = "Workflow {} stopped successfully";
    public static final String LOG_CREATED_RUN = "Created new WorkflowRun: {}";
    public static final String LOG_STOPPED_RUN = "Stopped WorkflowRun: {}";
    public static final String LOG_SKIPPING_STOPPED = "Skipping execution - Run {} is STOPPED";
    public static final String LOG_INTERRUPTING_THREAD = "Interrupting execution thread: {}";

    // --- Response Keys ---
    public static final String KEY_RUN_ID = "runId";
    public static final String KEY_WORKFLOW_ID = "workflowId";
    public static final String KEY_WORKFLOW_NAME = "workflowName";
    public static final String KEY_CURRENT_NODE_ID = "currentNodeId";
    public static final String KEY_OUTPUT = "output";
    public static final String KEY_EXECUTED_NODES = "executedNodes";
    public static final String KEY_SKIPPED = "skipped";
    public static final String KEY_REASON = "reason";
    public static final String KEY_SUCCESS = "success";
    public static final String KEY_RESULT = "result";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_ERROR = "error";
    public static final String KEY_STATUS = "status";
    public static final String KEY_IS_RUNNING = "isRunning";

    // --- Default Values ---
    public static final String DEFAULT_NULL = "null";
    public static final String DEFAULT_VERSION = "1.0.0";
    public static final String REASON_RUN_STOPPED = "Run was stopped";
    public static final String MSG_WORKFLOW_STOPPED = "Workflow stopped";
    public static final String MSG_EXECUTOR_FOR = "Node executor for ";

    // --- Engine Constants ---
    public static final String EXPR_VAR_INPUT = "input";
    public static final String EXPR_VAR_CTX = "ctx";

    // --- HTTP Constants ---
    public static final String HTTP_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_CONTENT_TYPE_JSON = "application/json";
    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_PUT = "PUT";
    public static final String HTTP_METHOD_DELETE = "DELETE";

    // --- Provider Constants ---
    public static final String PROVIDER_HTTP = "http";
    public static final String NAME_HTTP = "HTTP/REST API";
    public static final String PROVIDER_SLACK = "slack";
    public static final String NAME_SLACK = "Slack";
    public static final String PROVIDER_TEAMS = "teams";
    public static final String NAME_TEAMS = "Microsoft Teams";
    public static final String PROVIDER_WHATSAPP = "whatsapp";
    public static final String NAME_WHATSAPP = "WhatsApp";

    // --- Node Config Keys ---
    public static final String CFG_URL = "url";
    public static final String CFG_NODE_TYPE = "nodeType";
    public static final String CFG_BODY = "body";
    public static final String CFG_TO = "to";
    public static final String CFG_SUBJECT = "subject";
    public static final String CFG_MAPPING = "mapping";
    public static final String CFG_CRON = "cron";
    public static final String CFG_METHOD = "method";
    public static final String CFG_HEADERS = "headers";
    public static final String CFG_KEY = "key";
    public static final String CFG_VALUE = "value";
    public static final String CFG_KAFKA_MODE = "kafkaMode";

    // --- Node Types ---
    // Enums (TriggerNodeType, IntegrationNodeType) are used directly.

    public static final String KAFKA_MODE_CONSUMER = "CONSUMER";

    // --- Validation Constants ---
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";

    // --- Configuration Keys ---
    public static final String CFG_REQUIRED_FIELDS = "requiredFields";
    public static final String CFG_QUERY_PARAMS = "queryParams";
    public static final String CFG_WEBHOOK_URL = "webhookUrl";
    public static final String CFG_BOT_TOKEN = "botToken";
    public static final String CFG_CHANNEL = "channel";
    public static final String CFG_API_KEY = "apiKey";
    public static final String CFG_DEFAULT_PHONE_NUMBER = "defaultPhoneNumber";

    // --- Adapter Config Keys ---
    public static final String KEY_PHONE_NUMBER = "phoneNumber";
    public static final String KEY_WEBHOOK_URL = "webhookUrl";
    public static final String KEY_CHANNEL = "channel";

    // --- Default Mapping Fields ---
    public static final String DEFAULT_FIELD_NEW = "newField";
    public static final String DEFAULT_FIELD_OLD = "oldField";

}
