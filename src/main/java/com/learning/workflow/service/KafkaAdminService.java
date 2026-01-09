package com.learning.workflow.service;

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

/**
 * Service for Kafka admin operations: test connection, list/create topics.
 * Uses a connection cache to reuse AdminClient instances for the same
 * configuration.
 */
@Service
@Slf4j
public class KafkaAdminService {

    private static final int TIMEOUT_SECONDS = 10;

    // Cache AdminClient instances by config hash to avoid recreating connections
    private final Map<String, CachedAdminClient> adminClientCache = new ConcurrentHashMap<>();

    // Cache TTL in milliseconds (5 minutes)
    private static final long CACHE_TTL_MS = 5 * 60 * 1000;

    /**
     * Get or create AdminClient for the given configuration.
     * Cached clients are reused if config matches and TTL hasn't expired.
     */
    private AdminClient getOrCreateAdminClient(Map<String, Object> config) {
        String cacheKey = buildCacheKey(config);

        CachedAdminClient cached = adminClientCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.debug("Reusing cached AdminClient for: {}", config.get("bootstrapServers"));
            return cached.client;
        }

        // Close expired client if exists
        if (cached != null) {
            log.debug("Closing expired AdminClient for: {}", config.get("bootstrapServers"));
            try {
                cached.client.close();
            } catch (Exception e) {
                log.warn("Error closing expired AdminClient", e);
            }
        }

        log.info("Creating new AdminClient for: {}", config.get("bootstrapServers"));
        AdminClient newClient = AdminClient.create(buildAdminProperties(config));
        adminClientCache.put(cacheKey, new CachedAdminClient(newClient));

        return newClient;
    }

    /**
     * Build a cache key from configuration.
     * Key is based on connection-relevant properties only.
     */
    private String buildCacheKey(Map<String, Object> config) {
        return String.format("%s|%s|%s|%s",
                config.getOrDefault("bootstrapServers", ""),
                config.getOrDefault("securityProtocol", "PLAINTEXT"),
                config.getOrDefault("saslMechanism", ""),
                config.getOrDefault("sslKeystoreLocation", ""));
    }

    /**
     * Build Kafka AdminClient properties from config map.
     */
    private Properties buildAdminProperties(Map<String, Object> config) {
        Properties props = new Properties();

        String bootstrapServers = (String) config.getOrDefault("bootstrapServers", "localhost:9092");
        String securityProtocol = (String) config.getOrDefault("securityProtocol", "PLAINTEXT");

        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, TIMEOUT_SECONDS * 1000);
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, TIMEOUT_SECONDS * 1000);

        // Unique client ID to distinguish in logs
        props.put(AdminClientConfig.CLIENT_ID_CONFIG, "workflow-admin-" + bootstrapServers.hashCode());

        // SSL Configuration
        if ("SSL".equals(securityProtocol) || "SASL_SSL".equals(securityProtocol)) {
            if (config.containsKey("sslTruststoreLocation")) {
                props.put("ssl.truststore.location", config.get("sslTruststoreLocation"));
            }
            if (config.containsKey("sslTruststorePassword")) {
                props.put("ssl.truststore.password", config.get("sslTruststorePassword"));
            }
            if (config.containsKey("sslKeystoreLocation")) {
                props.put("ssl.keystore.location", config.get("sslKeystoreLocation"));
            }
            if (config.containsKey("sslKeystorePassword")) {
                props.put("ssl.keystore.password", config.get("sslKeystorePassword"));
            }
        }

        // SASL Configuration
        if ("SASL_PLAINTEXT".equals(securityProtocol) || "SASL_SSL".equals(securityProtocol)) {
            String saslMechanism = (String) config.getOrDefault("saslMechanism", "PLAIN");
            props.put("sasl.mechanism", saslMechanism);

            if (config.containsKey("saslJaasConfig")) {
                props.put("sasl.jaas.config", config.get("saslJaasConfig"));
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

            String clusterId = cluster.clusterId().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Collection<Node> nodes = cluster.nodes().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            List<String> brokers = nodes.stream()
                    .map(node -> node.host() + ":" + node.port())
                    .toList();

            result.put("success", true);
            result.put("clusterId", clusterId);
            result.put("brokers", brokers);
            result.put("brokerCount", brokers.size());

            log.info("Kafka connection test successful. Cluster: {}, Brokers: {}", clusterId, brokers);

        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Kafka connection test failed", e);
            result.put("success", false);
            result.put("error", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());

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
            Set<String> topics = topicsResult.names().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.debug("Listed {} topics from Kafka", topics.size());
            return topics;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Failed to list Kafka topics", e);
            invalidateCache(config);
            throw new RuntimeException("Failed to list topics: " + e.getMessage(), e);
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
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            result.put("success", true);
            result.put("topicName", topicName);
            result.put("partitions", partitions);
            result.put("replicationFactor", replicationFactor);

            log.info("Created Kafka topic: {} with {} partitions", topicName, partitions);

        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Failed to create Kafka topic: {}", topicName, e);
            result.put("success", false);
            result.put("error", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
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
            return System.currentTimeMillis() - createdAt > CACHE_TTL_MS;
        }
    }
}
