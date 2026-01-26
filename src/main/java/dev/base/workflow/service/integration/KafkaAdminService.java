package dev.base.workflow.service.integration;

import dev.base.workflow.model.dto.request.kafka.KafkaConnectionRequest;
import dev.base.workflow.model.dto.request.kafka.KafkaConnectionResponse;
import dev.base.workflow.model.dto.request.kafka.KafkaTopicRequest;
import dev.base.workflow.model.dto.request.kafka.KafkaTopicResponse;
import org.springframework.util.StringUtils;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.Node;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static dev.base.workflow.constant.KafkaConstants.*;

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
    private AdminClient getOrCreateAdminClient(KafkaConnectionRequest request) {
        String cacheKey = buildCacheKey(request);

        CachedAdminClient cached = adminClientCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.client;
        }

        // Close expired client if exists
        if (cached != null) {
            try {
                cached.client.close();
            } catch (Exception e) {
                log.warn("Error closing expired AdminClient", e);
            }
        }

        log.info("Creating new AdminClient for: {}", request.getBootstrapServers());
        AdminClient newClient = AdminClient.create(buildAdminProperties(request));
        adminClientCache.put(cacheKey, new CachedAdminClient(newClient));

        return newClient;
    }

    private String buildCacheKey(KafkaConnectionRequest request) {
        return request.getBootstrapServers() + "|" +
                request.getSecurityProtocol() + "|" +
                request.getSaslMechanism() + "|" +
                request.getSaslJaasConfig() + "|" +
                request.getSslKeyStoreLocation();
    }

    private Properties buildAdminProperties(KafkaConnectionRequest request) {
        Properties props = new Properties();

        String bootstrapServers = StringUtils.hasText(request.getBootstrapServers()) ? request.getBootstrapServers()
                : DEFAULT_BOOTSTRAP_SERVERS;
        String securityProtocol = StringUtils.hasText(request.getSecurityProtocol()) ? request.getSecurityProtocol()
                : VAL_SEC_PROTO_PLAINTEXT;

        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, KAFKA_TIMEOUT_SECONDS * 1000);
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, KAFKA_TIMEOUT_SECONDS * 1000);
        props.put(AdminClientConfig.CLIENT_ID_CONFIG, CLIENT_ID_PREFIX + bootstrapServers.hashCode());

        // SSL Configuration
        if (VAL_SEC_PROTO_SSL.equals(securityProtocol) || VAL_SEC_PROTO_SASL_SSL.equals(securityProtocol)) {
            if (StringUtils.hasText(request.getSslTrustStoreLocation())) {
                props.put(PROP_SSL_TRUSTSTORE_LOC, request.getSslTrustStoreLocation());
            }
            if (StringUtils.hasText(request.getSslTrustStorePassword())) {
                props.put(PROP_SSL_TRUSTSTORE_PWD, request.getSslTrustStorePassword());
            }
            if (StringUtils.hasText(request.getSslKeyStoreLocation())) {
                props.put(PROP_SSL_KEYSTORE_LOC, request.getSslKeyStoreLocation());
            }
            if (StringUtils.hasText(request.getSslKeyStorePassword())) {
                props.put(PROP_SSL_KEYSTORE_PWD, request.getSslKeyStorePassword());
            }
        }

        // SASL Configuration
        if (VAL_SEC_PROTO_SASL_PLAINTEXT.equals(securityProtocol) || VAL_SEC_PROTO_SASL_SSL.equals(securityProtocol)) {
            String saslMechanism = StringUtils.hasText(request.getSaslMechanism()) ? request.getSaslMechanism()
                    : VAL_SASL_MECH_PLAIN;
            props.put(PROP_SASL_MECHANISM, saslMechanism);

            if (StringUtils.hasText(request.getSaslJaasConfig())) {
                props.put(PROP_SASL_JAAS_CONFIG, request.getSaslJaasConfig());
            }
        }

        return props;
    }

    /**
     * Test connection to Kafka cluster.
     */
    public KafkaConnectionResponse testConnection(KafkaConnectionRequest request) {
        try {
            AdminClient adminClient = getOrCreateAdminClient(request);
            DescribeClusterResult cluster = adminClient.describeCluster();

            String clusterId = cluster.clusterId().get(KAFKA_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Collection<Node> nodes = cluster.nodes().get(KAFKA_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            List<String> brokers = nodes.stream()
                    .map(node -> node.host() + ":" + node.port())
                    .toList();

            log.info("Kafka connection test successful. Cluster: {}, Brokers: {}", clusterId, brokers);

            return KafkaConnectionResponse.builder()
                    .success(true)
                    .clusterId(clusterId)
                    .brokers(brokers)
                    .brokerCount(brokers.size())
                    .build();

        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Kafka connection test failed", e);
            invalidateCache(request);

            return KafkaConnectionResponse.builder()
                    .success(false)
                    .error(e.getCause() != null ? e.getCause().getMessage() : e.getMessage())
                    .build();
        }
    }

    /**
     * List all topics in the Kafka cluster.
     */
    public Set<String> listTopics(KafkaConnectionRequest request) {
        try {
            AdminClient adminClient = getOrCreateAdminClient(request);
            ListTopicsResult topicsResult = adminClient.listTopics();
            return topicsResult.names().get(KAFKA_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Failed to list Kafka topics", e);
            invalidateCache(request);
            throw new RuntimeException("Failed to list topics: " + e.getMessage(), e);
        }
    }

    /**
     * Create a new topic in Kafka.
     */
    public KafkaTopicResponse createTopic(KafkaTopicRequest request) {
        String topicName = request.getTopicName();
        try {
            AdminClient adminClient = getOrCreateAdminClient(request);
            NewTopic newTopic = new NewTopic(topicName, request.getPartitions(), request.getReplicationFactor());
            adminClient.createTopics(Collections.singleton(newTopic))
                    .all()
                    .get(KAFKA_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            log.info("Created Kafka topic: {}", topicName);

            return KafkaTopicResponse.builder()
                    .success(true)
                    .topicName(topicName)
                    .partitions(request.getPartitions())
                    .replicationFactor(request.getReplicationFactor())
                    .build();

        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Failed to create Kafka topic: {}", topicName, e);
            return KafkaTopicResponse.builder()
                    .success(false)
                    .error(e.getCause() != null ? e.getCause().getMessage() : e.getMessage())
                    .build();
        }
    }

    private void invalidateCache(KafkaConnectionRequest request) {
        String cacheKey = buildCacheKey(request);
        CachedAdminClient cached = adminClientCache.remove(cacheKey);
        if (cached != null) {
            try {
                cached.client.close();
            } catch (Exception e) {
                log.warn("Error closing AdminClient during cache invalidation", e);
            }
        }
    }

    @PreDestroy
    public void cleanup() {
        adminClientCache.values().forEach(cached -> {
            try {
                cached.client.close();
            } catch (Exception e) {
                log.warn("Error closing AdminClient during cleanup", e);
            }
        });
        adminClientCache.clear();
    }

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
