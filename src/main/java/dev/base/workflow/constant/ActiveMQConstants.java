package dev.base.workflow.constant;

public class ActiveMQConstants {
    public static final String CFG_BROKER_URL = "brokerUrl";
    public static final String CFG_DESTINATION_TYPE = "destinationType"; // QUEUE or TOPIC
    public static final String CFG_DESTINATION_NAME = "destinationName";
    public static final String CFG_USERNAME = "username";
    public static final String CFG_PASSWORD = "password";
    public static final String CFG_MESSAGE_BODY = "messageBody";

    public static final String DESTINATION_TYPE_QUEUE = "QUEUE";
    public static final String DESTINATION_TYPE_TOPIC = "TOPIC";

    public static final String CFG_SSL_ENABLED = "sslEnabled";
    public static final String CFG_SSL_TRUSTSTORE_LOC = "sslTrustStoreLocation";
    public static final String CFG_SSL_TRUSTSTORE_PWD = "sslTrustStorePassword";
    public static final String CFG_SSL_KEYSTORE_LOC = "sslKeyStoreLocation";
    public static final String CFG_SSL_KEYSTORE_PWD = "sslKeyStorePassword";

    // Internal use for testing/listing
    public static final String KEY_QUEUES = "queues";
    public static final String KEY_TOPICS = "topics";

    public static final String ERR_BROKER_URL_REQUIRED = "ActiveMQ Broker URL is required";
    public static final String ERR_DESTINATION_NAME_REQUIRED = "ActiveMQ Destination Name is required";

    public static final String CFG_ACTIVEMQ_MODE = "activeMQMode";
    public static final String MODE_PRODUCER = "PRODUCER";
    public static final String MODE_CONSUMER = "CONSUMER";

    public static final String CFG_POLL_TIMEOUT_MS = "pollTimeoutMs";
    public static final String CFG_BATCH_SIZE = "batchSize";
}
