package dev.base.workflow.constant;

/**
 * Kafka-specific constants.
 */
public final class KafkaConstants {

    private KafkaConstants() {
        // Prevent instantiation
    }

    // --- Kafka Constants ---
    public static final int KAFKA_TIMEOUT_SECONDS = 10;
    public static final long KAFKA_CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

    // --- Kafka Configuration Keys ---
    public static final String CFG_BOOTSTRAP_SERVERS = "bootstrapServers";
    public static final String CLIENT_ID_PREFIX = "workflow-admin-";
    public static final String CFG_TOPIC = "topic";
    public static final String CFG_TOPIC_NAME = "topicName";
    public static final String CFG_PARTITIONS = "partitions";
    public static final String CFG_REPLICATION_FACTOR = "replicationFactor";
    public static final String CFG_POLL_TIMEOUT_MS = "pollTimeoutMs";
    public static final String CFG_MESSAGE_TEMPLATE = "message";
    public static final String CFG_MESSAGE_KEY = "messageKey";
    public static final String CFG_KAFKA_MODE = "kafkaMode";
    public static final String CFG_SECURITY_PROTOCOL = "securityProtocol";
    public static final String CFG_CONSUMER_GROUP = "consumerGroup";
    public static final String CFG_SASL_MECHANISM = "saslMechanism";

    // --- Kafka Security Config Keys ---
    public static final String CFG_SSL_TRUSTSTORE_LOC = "sslTruststoreLocation";
    public static final String CFG_SSL_TRUSTSTORE_PWD = "sslTruststorePassword";
    public static final String CFG_SSL_KEYSTORE_LOC = "sslKeystoreLocation";
    public static final String CFG_SSL_KEYSTORE_PWD = "sslKeystorePassword";
    public static final String CFG_SASL_JAAS_CONFIG = "saslJaasConfig";

    // --- Kafka Properties ---
    public static final String PROP_SSL_TRUSTSTORE_LOC = "ssl.truststore.location";
    public static final String PROP_SSL_TRUSTSTORE_PWD = "ssl.truststore.password";
    public static final String PROP_SSL_KEYSTORE_LOC = "ssl.keystore.location";
    public static final String PROP_SSL_KEYSTORE_PWD = "ssl.keystore.password";
    public static final String PROP_SASL_JAAS_CONFIG = "sasl.jaas.config";
    public static final String PROP_SASL_MECHANISM = "sasl.mechanism";

    // --- Kafka Modes ---
    public static final String MODE_PRODUCER = "PRODUCER";
    public static final String MODE_CONSUMER = "CONSUMER";

    // --- Kafka Values ---
    public static final String VAL_SEC_PROTO_PLAINTEXT = "PLAINTEXT";
    public static final String VAL_SEC_PROTO_SSL = "SSL";
    public static final String VAL_SEC_PROTO_SASL_SSL = "SASL_SSL";
    public static final String VAL_SEC_PROTO_SASL_PLAINTEXT = "SASL_PLAINTEXT";
    public static final String VAL_SASL_MECH_PLAIN = "PLAIN";
    public static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";

    // --- Kafka Response Keys ---
    public static final String KEY_CLUSTER_ID = "clusterId";
    public static final String KEY_BROKERS = "brokers";
    public static final String KEY_BROKER_COUNT = "brokerCount";
    public static final String KEY_RECORDS = "records";
    public static final String KEY_COUNT = "count";
    public static final String KEY_OFFSET = "offset";
    public static final String KEY_PARTITION = "partition";
    public static final String KEY_MESSAGES = "messages";
    public static final String KEY_MESSAGE_COUNT = "messageCount";
    public static final String KEY_TIMESTAMP = "timestamp";

}
