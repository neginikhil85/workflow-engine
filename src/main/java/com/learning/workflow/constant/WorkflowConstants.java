package com.learning.workflow.constant;

/**
 * Application-wide constants.
 * Centralizes error messages, log prefixes, and magic numbers.
 */
public final class WorkflowConstants {

    private WorkflowConstants() {
        // Prevent instantiation
    }

    // --- Error Messages ---
    public static final String ERR_WORKFLOW_NOT_FOUND = "Workflow not found: ";
    public static final String ERR_RUN_NOT_FOUND = "Run not found: ";
    public static final String ERR_EXECUTION_CANCELLED = "Workflow execution was cancelled";
    public static final String ERR_STOPPED_BY_USER = "Stopped by user";

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
    public static final String REASON_RUN_STOPPED = "Run was stopped";
    public static final String MSG_WORKFLOW_STOPPED = "Workflow stopped";

    // --- Engine Constants ---
    public static final String ERR_NO_EXECUTOR = "No executor registered for nodeType: ";
    public static final String EXPR_VAR_INPUT = "input";
    public static final String EXPR_VAR_CTX = "ctx";

    // --- Kafka Constants ---
    public static final int KAFKA_TIMEOUT_SECONDS = 10;
    public static final long KAFKA_CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

    // --- HTTP Constants ---
    public static final String HTTP_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_CONTENT_TYPE_JSON = "application/json";

    // --- Node Config Keys ---
    public static final String CFG_URL = "url";
    public static final String CFG_BODY = "body";
    public static final String CFG_TO = "to";
    public static final String CFG_SUBJECT = "subject";
    public static final String CFG_MAPPING = "mapping";
    public static final String CFG_CRON = "cron";
    public static final String CFG_METHOD = "method";
    public static final String CFG_HEADERS = "headers";
    public static final String CFG_KEY = "key";
    public static final String CFG_VALUE = "value";

    // --- Validation Constants ---
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";

}
