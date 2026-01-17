package dev.base.workflow.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.Node;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static dev.base.workflow.constant.KafkaConstants.*;
import static dev.base.workflow.constant.WorkflowConstants.KEY_SUCCESS;
import static dev.base.workflow.constant.WorkflowConstants.KEY_ERROR;

import dev.base.workflow.util.StringUtils;

/**
 * Service for Kafka admin operations: test connection, list/create topics.
 * Uses a connection cache to reuse AdminClient instances for the same
 * configuration.
 */
@Service
@Slf4j
public class KafkaAdminService {

    // Cache AdminClient instances by config hash to avoid recreating connections
    private final Map<String, CachedAdminClient> adminClientCache = new ConcurrentHashMap<>();

    /**
     * Get or create AdminClient for the given configuration.
     * Cached clients are reused if config matches and TTL hasn't expired.
     */
    private AdminClient getOrCreateAdminClient(Map<String, Object> config) {
        String cacheKey = buildCacheKey(config);

        CachedAdminClient cached = adminClientCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.debug("Reusing cached AdminClient for: {}", config.get(CFG_BOOTSTRAP_SERVERS));
            return cached.client;
        }

        // Close expired client if exists
        if (cached != null) {
            log.debug("Closing expired AdminClient for: {}", config.get(CFG_BOOTSTRAP_SERVERS));
            try {
                cached.client.close();
            } catch (Exception e) {
                log.warn("Error closing expired AdminClient", e);
            }
        }

        log.info("Creating new AdminClient for: {}", config.get(CFG_BOOTSTRAP_SERVERS));
        AdminClient newClient = AdminClient.create(buildAdminProperties(config));
        adminClientCache.put(cacheKey, new CachedAdminClient(newClient));

        return newClient;
    }

    /**
     * Build a cache key from configuration.
     * Key is based on connection-relevant properties only.
     */
    private String buildCacheKey(Map<String, Object> config) {
        return StringUtils.concat(config.get(CFG_BOOTSTRAP_SERVERS), "|",
                config.get(CFG_SECURITY_PROTOCOL), "|",
                config.get(CFG_SASL_MECHANISM), "|",
                config.get(CFG_SASL_JAAS_CONFIG), "|",
                config.get(CFG_SSL_KEYSTORE_LOC));
    }

    /**
     * Build Kafka AdminClient properties from config map.
     */
    private Properties buildAdminProperties(Map<String, Object> config) {
        Properties props = new Properties();

        String bootstrapServers = (String) config.getOrDefault(CFG_BOOTSTRAP_SERVERS, DEFAULT_BOOTSTRAP_SERVERS);
        String securityProtocol = (String) config.getOrDefault(CFG_SECURITY_PROTOCOL, VAL_SEC_PROTO_PLAINTEXT);

        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, KAFKA_TIMEOUT_SECONDS * 1000);
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, KAFKA_TIMEOUT_SECONDS * 1000);

        // Unique client ID to distinguish in logs
        props.put(AdminClientConfig.CLIENT_ID_CONFIG, CLIENT_ID_PREFIX + bootstrapServers.hashCode());

        // SSL Configuration
        if (VAL_SEC_PROTO_SSL.equals(securityProtocol) || VAL_SEC_PROTO_SASL_SSL.equals(securityProtocol)) {
            if (config.containsKey(CFG_SSL_TRUSTSTORE_LOC)) {
                props.put(PROP_SSL_TRUSTSTORE_LOC, config.get(CFG_SSL_TRUSTSTORE_LOC));
            }
            if (config.containsKey(CFG_SSL_TRUSTSTORE_PWD)) {
                props.put(PROP_SSL_TRUSTSTORE_PWD, config.get(CFG_SSL_TRUSTSTORE_PWD));
            }
            if (config.containsKey(CFG_SSL_KEYSTORE_LOC)) {
                props.put(PROP_SSL_KEYSTORE_LOC, config.get(CFG_SSL_KEYSTORE_LOC));
            }
            if (config.containsKey(CFG_SSL_KEYSTORE_PWD)) {
                props.put(PROP_SSL_KEYSTORE_PWD, config.get(CFG_SSL_KEYSTORE_PWD));
            }
        }

        // SASL Configuration
        if (VAL_SEC_PROTO_SASL_PLAINTEXT.equals(securityProtocol) || VAL_SEC_PROTO_SASL_SSL.equals(securityProtocol)) {
            String saslMechanism = (String) config.getOrDefault(CFG_SASL_MECHANISM, VAL_SASL_MECH_PLAIN);
            props.put(PROP_SASL_MECHANISM, saslMechanism);

            if (config.containsKey(CFG_SASL_JAAS_CONFIG)) {
                props.put(PROP_SASL_JAAS_CONFIG, config.get(CFG_SASL_JAAS_CONFIG));
            }
        }

        return props;
    }

    /**
     * Test connection to Kafka cluster.
     *
     * @return Map with success status, clusterId, and broker list
     */
    public Map<String, Object> testConnection(Map<String, Object> config) {
        Map<String, Object> result = new HashMap<>();

        try {
            AdminClient adminClient = getOrCreateAdminClient(config);
            DescribeClusterResult cluster = adminClient.describeCluster();

            String clusterId = cluster.clusterId().get(KAFKA_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Collection<Node> nodes = cluster.nodes().get(KAFKA_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            List<String> brokers = nodes.stream()
                    .map(node -> StringUtils.concat(node.host(), ":", node.port()))
                    .toList();

            result.put(KEY_SUCCESS, true);
            result.put(KEY_CLUSTER_ID, clusterId);
            result.put(KEY_BROKERS, brokers);
            result.put(KEY_BROKER_COUNT, brokers.size());

            log.info("Kafka connection test successful. Cluster: {}, Brokers: {}", clusterId, brokers);

        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Kafka connection test failed", e);
            result.put(KEY_SUCCESS, false);
            result.put(KEY_ERROR, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());

            // Invalidate cache on connection failure
            invalidateCache(config);
        }

        return result;
    }

    /**
     * List all topics in the Kafka cluster.
     */
    public Set<String> listTopics(Map<String, Object> config) {
        try {
            AdminClient adminClient = getOrCreateAdminClient(config);
            ListTopicsResult topicsResult = adminClient.listTopics();
            Set<String> topics = topicsResult.names().get(KAFKA_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.debug("Listed {} topics from Kafka", topics.size());
            return topics;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Failed to list Kafka topics", e);
            invalidateCache(config);
            throw new RuntimeException(StringUtils.concat("Failed to list topics: ", e.getMessage()), e);
        }
    }

    /**
     * Create a new topic in Kafka.
     */
    public Map<String, Object> createTopic(Map<String, Object> config, String topicName, int partitions,
            short replicationFactor) {
        Map<String, Object> result = new HashMap<>();

        try {
            AdminClient adminClient = getOrCreateAdminClient(config);
            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
            adminClient.createTopics(Collections.singleton(newTopic))
                    .all()
                    .get(KAFKA_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            result.put(KEY_SUCCESS, true);
            result.put(CFG_TOPIC_NAME, topicName);
            result.put(CFG_PARTITIONS, partitions);
            result.put(CFG_REPLICATION_FACTOR, replicationFactor);

            log.info("Created Kafka topic: {} with {} partitions", topicName, partitions);

        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Failed to create Kafka topic: {}", topicName, e);
            result.put(KEY_SUCCESS, false);
            result.put(KEY_ERROR, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }

        return result;
    }

    /**
     * Invalidate cached client for a given configuration.
     */
    private void invalidateCache(Map<String, Object> config) {
        String cacheKey = buildCacheKey(config);
        CachedAdminClient cached = adminClientCache.remove(cacheKey);
        if (cached != null) {
            try {
                cached.client.close();
            } catch (Exception e) {
                log.warn("Error closing AdminClient during cache invalidation", e);
            }
        }
    }

    /**
     * Clean up all cached clients on shutdown.
     */
    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up {} cached Kafka AdminClients", adminClientCache.size());
        adminClientCache.values().forEach(cached -> {
            try {
                cached.client.close();
            } catch (Exception e) {
                log.warn("Error closing AdminClient during cleanup", e);
            }
        });
        adminClientCache.clear();
    }

    /**
     * Wrapper class for cached AdminClient with creation timestamp.
     */
    private static class CachedAdminClient {
        final AdminClient client;
        final long createdAt;

        CachedAdminClient(AdminClient client) {
            this.client = client;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > KAFKA_CACHE_TTL_MS;
        }
    }
}
