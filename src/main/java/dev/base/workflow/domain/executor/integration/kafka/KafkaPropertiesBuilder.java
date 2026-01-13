package dev.base.workflow.domain.executor.integration.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

import static dev.base.workflow.constant.KafkaConstants.*;

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

        String bootstrapServers = (String) config.getOrDefault(CFG_BOOTSTRAP_SERVERS, "localhost:9092");
        String securityProtocol = (String) config.getOrDefault(CFG_SECURITY_PROTOCOL, VAL_SEC_PROTO_PLAINTEXT);

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

        String bootstrapServers = (String) config.getOrDefault(CFG_BOOTSTRAP_SERVERS, "localhost:9092");
        String securityProtocol = (String) config.getOrDefault(CFG_SECURITY_PROTOCOL, VAL_SEC_PROTO_PLAINTEXT);

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
        if ("SASL_PLAINTEXT".equals(securityProtocol) || "SASL_SSL".equals(securityProtocol)) {
            String saslMechanism = (String) config.getOrDefault("saslMechanism", "PLAIN");
            props.put("sasl.mechanism", saslMechanism);

            if (config.containsKey(CFG_SASL_JAAS_CONFIG)) {
                props.put(PROP_SASL_JAAS_CONFIG, config.get(CFG_SASL_JAAS_CONFIG));
            }
        }
    }
}
