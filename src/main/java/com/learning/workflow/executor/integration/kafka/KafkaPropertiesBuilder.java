package com.learning.workflow.executor.integration.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

/**
 * Builds Kafka producer and consumer properties from node configuration.
 * Single Responsibility: Only handles property building and security
 * configuration.
 */
@Component
@Slf4j
public class KafkaPropertiesBuilder {

    /**
     * Builds properties for Kafka Producer.
     */
    public Properties buildProducerProperties(Map<String, Object> config) {
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

    /**
     * Builds properties for Kafka Consumer.
     */
    public Properties buildConsumerProperties(Map<String, Object> config, String consumerGroup) {
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

    /**
     * Adds SSL and SASL security properties based on the security protocol.
     */
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
}
