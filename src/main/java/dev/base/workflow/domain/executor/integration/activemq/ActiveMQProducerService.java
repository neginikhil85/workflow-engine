package dev.base.workflow.domain.executor.integration.activemq;

import dev.base.workflow.domain.engine.ExpressionEvaluator;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import org.springframework.util.StringUtils;

import jakarta.jms.JMSException;
import jakarta.jms.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import java.util.Map;

import static dev.base.workflow.constant.ActiveMQConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActiveMQProducerService {

    private final ExpressionEvaluator expressionEvaluator;

    public NodeExecutionResult produce(NodeDefinition node, Object input, Map<String, Object> config,
            ExecutionContext ctx) {
        String brokerUrl = (String) config.get(CFG_BROKER_URL);
        String destinationType = (String) config.getOrDefault(CFG_DESTINATION_TYPE, DESTINATION_TYPE_QUEUE);
        String destinationName = (String) config.get(CFG_DESTINATION_NAME);
        String username = (String) config.get(CFG_USERNAME);
        String password = (String) config.get(CFG_PASSWORD);

        // Evaluate message body expression
        String messageTemplate = (String) config.getOrDefault(CFG_MESSAGE_BODY, "{}");
        String messageBody = expressionEvaluator.parseTemplate(messageTemplate, input, ctx);

        log.info("Sending message to ActiveMQ {} [{}]: {}", destinationType, destinationName, messageBody);

        try {
            // Create connection factory per request (or cache it if needed later)
            // For now, simple approach: new factory per request
            ActiveMQConnectionFactory connectionFactory;
            boolean sslEnabled = Boolean.TRUE.equals(config.get(CFG_SSL_ENABLED));

            if (sslEnabled) {
                ActiveMQSslConnectionFactory sslFactory = new ActiveMQSslConnectionFactory(brokerUrl);
                // Configure SSL properties
                String trustStore = (String) config.get(CFG_SSL_TRUSTSTORE_LOC);
                String trustStorePwd = (String) config.get(CFG_SSL_TRUSTSTORE_PWD);
                String keyStore = (String) config.get(CFG_SSL_KEYSTORE_LOC);
                String keyStorePwd = (String) config.get(CFG_SSL_KEYSTORE_PWD);

                if (StringUtils.hasText(trustStore))
                    sslFactory.setTrustStore(trustStore);
                if (StringUtils.hasText(trustStorePwd))
                    sslFactory.setTrustStorePassword(trustStorePwd);
                if (StringUtils.hasText(keyStore))
                    sslFactory.setKeyStore(keyStore);
                if (StringUtils.hasText(keyStorePwd))
                    sslFactory.setKeyStorePassword(keyStorePwd);

                connectionFactory = sslFactory;
            } else {
                connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            }

            if (StringUtils.hasText(username)) {
                connectionFactory.setUserName(username);
            }
            if (StringUtils.hasText(password)) {
                connectionFactory.setPassword(password);
            }

            JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);

            // Set Pub/Sub domain if Topic
            boolean isTopic = DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType);
            jmsTemplate.setPubSubDomain(isTopic);

            jmsTemplate.send(destinationName, new MessageCreator() {
                @Override
                public jakarta.jms.Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(messageBody);
                }
            });

            return NodeExecutionResult.success(node.getId(), Map.of(
                    "status", "SENT",
                    "destination", destinationName,
                    "type", destinationType));

        } catch (Exception e) {
            log.error("Failed to send message to ActiveMQ", e);
            throw new RuntimeException("ActiveMQ Send Error: " + e.getMessage(), e);
        }
    }
}
