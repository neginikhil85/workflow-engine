package com.learning.workflow.executor.integration;

import com.learning.workflow.engine.NodeExecutor;
import com.learning.workflow.model.core.ExecutionContext;
import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.core.NodeExecutionResult;
import com.learning.workflow.model.nodetype.IntegrationNodeType;
import com.learning.workflow.model.nodetype.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Kafka node executor supporting both Producer and Consumer modes.
 */
@Component
@Slf4j
public class KafkaExecutor implements NodeExecutor {

    @Override
    public NodeType getSupportedNodeType() {
        return IntegrationNodeType.KAFKA;
    }

    @Override
    public NodeExecutionResult execute(NodeDefinition node, Object input, ExecutionContext ctx) {
        Map<String, Object> config = node.getConfig();

        if (config == null) {
            throw new IllegalArgumentException("Kafka node configuration is missing");
        }

        String mode = (String) config.getOrDefault("kafkaMode", "PRODUCER");

        log.info("Executing Kafka node in {} mode", mode);

        if ("PRODUCER".equalsIgnoreCase(mode)) {
            return executeProducer(node, input, config);
        } else if ("CONSUMER".equalsIgnoreCase(mode)) {
            return executeConsumer(node, config);
        } else {
            throw new IllegalArgumentException("Invalid Kafka mode: " + mode + ". Must be PRODUCER or CONSUMER");
        }
    }

    private NodeExecutionResult executeProducer(NodeDefinition node, Object input, Map<String, Object> config) {
        String topic = (String) config.get("topic");
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("Kafka topic is required for producer");
        }

        // Get message - can be from config or from input
        String message = (String) config.get("message");
        if (message == null && input != null) {
            message = input.toString();
        }
        if (message == null) {
            message = "{}";
        }

        Properties props = buildProducerProperties(config);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);

            // Optional: message key for partitioning
            String messageKey = (String) config.get("messageKey");
            if (messageKey != null && !messageKey.isBlank()) {
                record = new ProducerRecord<>(topic, messageKey, message);
            }

            Future<RecordMetadata> future = producer.send(record);
            RecordMetadata metadata = future.get(10, TimeUnit.SECONDS);

            log.info("Kafka message sent to topic: {} partition: {} offset: {}",
                    metadata.topic(), metadata.partition(), metadata.offset());

            return NodeExecutionResult.success(node.getId(), Map.of(
                    "status", "sent",
                    "topic", metadata.topic(),
                    "partition", metadata.partition(),
                    "offset", metadata.offset(),
                    "timestamp", metadata.timestamp()));

        } catch (Exception e) {
            log.error("Failed to send Kafka message", e);
            throw new RuntimeException("Failed to produce message to Kafka: " + e.getMessage(), e);
        }
    }

    private NodeExecutionResult executeConsumer(NodeDefinition node, Map<String, Object> config) {
        String topic = (String) config.get("topic");
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("Kafka topic is required for consumer");
        }

        String consumerGroup = (String) config.getOrDefault("consumerGroup", "workflow-consumer-" + UUID.randomUUID());
        int pollTimeout = config.containsKey("pollTimeoutMs")
                ? ((Number) config.get("pollTimeoutMs")).intValue()
                : 5000;

        Properties props = buildConsumerProperties(config, consumerGroup);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            log.info("Polling Kafka topic: {} for {} ms", topic, pollTimeout);

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pollTimeout));

            List<Map<String, Object>> messages = new ArrayList<>();
            for (ConsumerRecord<String, String> record : records) {
                messages.add(Map.of(
                        "key", record.key() != null ? record.key() : "",
                        "value", record.value(),
                        "partition", record.partition(),
                        "offset", record.offset(),
                        "timestamp", record.timestamp()));
            }

            log.info("Consumed {} messages from Kafka topic: {}", messages.size(), topic);

            return NodeExecutionResult.success(node.getId(), Map.of(
                    "status", "consumed",
                    "topic", topic,
                    "messageCount", messages.size(),
                    "messages", messages));

        } catch (Exception e) {
            log.error("Failed to consume from Kafka", e);
            throw new RuntimeException("Failed to consume from Kafka: " + e.getMessage(), e);
        }
    }

    private Properties buildProducerProperties(Map<String, Object> config) {
        Properties props = new Properties();

        String bootstrapServers = (String) config.getOrDefault("bootstrapServers", "localhost:9092");
        String securityProtocol = (String) config.getOrDefault("securityProtocol", "PLAINTEXT");

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put("security.protocol", securityProtocol);

        addSecurityProperties(props, config, securityProtocol);

        return props;
    }

    private Properties buildConsumerProperties(Map<String, Object> config, String consumerGroup) {
        Properties props = new Properties();

        String bootstrapServers = (String) config.getOrDefault("bootstrapServers", "localhost:9092");
        String securityProtocol = (String) config.getOrDefault("securityProtocol", "PLAINTEXT");

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put("security.protocol", securityProtocol);

        addSecurityProperties(props, config, securityProtocol);

        return props;
    }

    private void addSecurityProperties(Properties props, Map<String, Object> config, String securityProtocol) {
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
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        return Map.of(
                "bootstrapServers", "localhost:9092",
                "securityProtocol", "PLAINTEXT",
                "kafkaMode", "PRODUCER",
                "topic", "",
                "message", "{}",
                "consumerGroup", "workflow-consumer-group",
                "pollTimeoutMs", 5000);
    }

    @Override
    public void validate(NodeDefinition node) {
        Map<String, Object> config = node.getConfig();
        if (config == null) {
            throw new IllegalArgumentException("Kafka configuration is required");
        }

        String topic = (String) config.get("topic");
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("Kafka topic is required");
        }

        String bootstrapServers = (String) config.get("bootstrapServers");
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            throw new IllegalArgumentException("Bootstrap servers are required");
        }
    }
}
