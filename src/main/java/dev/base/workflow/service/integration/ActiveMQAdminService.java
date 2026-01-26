package dev.base.workflow.service.integration;

import dev.base.workflow.model.dto.request.activemq.ActiveMQConnectionRequest;
import dev.base.workflow.model.dto.request.activemq.ActiveMQConnectionResponse;
import org.springframework.util.StringUtils;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static dev.base.workflow.constant.ActiveMQConstants.DESTINATION_TYPE_QUEUE;
import static dev.base.workflow.constant.ActiveMQConstants.DESTINATION_TYPE_TOPIC;

/**
 * Service for ActiveMQ admin operations: test connection, list destinations.
 * Uses a caching mechanism similar to KafkaAdminService.
 */
@Service
@Slf4j
public class ActiveMQAdminService {

    /**
     * Test connection to ActiveMQ broker.
     */
    public ActiveMQConnectionResponse testConnection(ActiveMQConnectionRequest request) {
        Connection connection = null;

        try {
            ActiveMQConnectionFactory factory = createConnectionFactory(request);
            connection = factory.createConnection();
            connection.start();

            // connection.getMetaData() can provide some info
            String providerVersion = connection.getMetaData().getProviderVersion();

            log.info("ActiveMQ connection test successful. Version: {}", providerVersion);

            return ActiveMQConnectionResponse.builder()
                    .success(true)
                    .providerVersion(providerVersion)
                    .build();

        } catch (Exception e) {
            log.error("ActiveMQ connection test failed", e);
            throw new IllegalArgumentException("ActiveMQ Connection Failed: " + e.getMessage());
        } finally {
            closeQuietly(connection);
        }
    }

    /**
     * List destinations (Queues and Topics) from ActiveMQ.
     * Uses DestinationSource which subscribes to Advisory topics.
     * Filter by destinationType if provided in config.
     */
    public List<String> listDestinations(ActiveMQConnectionRequest request) {
        Connection connection = null;
        List<String> result = new ArrayList<>();
        String type = request.getDestinationType(); // Helper to filter if needed

        try {
            ActiveMQConnectionFactory factory = createConnectionFactory(request);
            connection = factory.createConnection();
            connection.start();

            if (connection instanceof ActiveMQConnection) {
                ActiveMQConnection amqConnection = (ActiveMQConnection) connection;
                DestinationSource destinationSource = amqConnection.getDestinationSource();

                destinationSource.start();
                Thread.sleep(200);

                if (DESTINATION_TYPE_QUEUE.equalsIgnoreCase(type) || !StringUtils.hasText(type)) {
                    result.addAll(destinationSource.getQueues().stream()
                            .map(ActiveMQQueue::getPhysicalName)
                            .sorted()
                            .toList());
                }

                if (DESTINATION_TYPE_TOPIC.equalsIgnoreCase(type) || !StringUtils.hasText(type)) {
                    result.addAll(destinationSource.getTopics().stream()
                            .map(ActiveMQTopic::getPhysicalName)
                            .sorted()
                            .toList());
                }

                log.info("Listed {} destinations from ActiveMQ", result.size());
            }

        } catch (Exception e) {
            log.error("Failed to list ActiveMQ destinations", e);
            throw new RuntimeException("Failed to list destinations: " + e.getMessage());
        } finally {
            closeQuietly(connection);
        }

        return result;
    }

    /**
     * Create a simple connection factory based on config.
     */
    private ActiveMQConnectionFactory createConnectionFactory(ActiveMQConnectionRequest request) {
        String brokerUrl = request.getBrokerUrl();
        String username = request.getUsername();
        String password = request.getPassword();
        boolean sslEnabled = request.isSslEnabled();

        ActiveMQConnectionFactory factory;

        if (sslEnabled) {
            ActiveMQSslConnectionFactory sslFactory = new ActiveMQSslConnectionFactory(brokerUrl);
            configureSsl(sslFactory, request);
            factory = sslFactory;
        } else {
            factory = new ActiveMQConnectionFactory(brokerUrl);
        }

        if (StringUtils.hasText(username)) {
            factory.setUserName(username);
        }
        if (StringUtils.hasText(password)) {
            factory.setPassword(password);
        }

        return factory;
    }

    private void configureSsl(ActiveMQSslConnectionFactory factory, ActiveMQConnectionRequest request) {
        try {
            String trustStore = request.getSslTrustStoreLocation();
            String trustStorePwd = request.getSslTrustStorePassword();
            String keyStore = request.getSslKeyStoreLocation();
            String keyStorePwd = request.getSslKeyStorePassword();

            if (StringUtils.hasText(trustStore)) {
                factory.setTrustStore(trustStore);
            }
            if (StringUtils.hasText(trustStorePwd)) {
                factory.setTrustStorePassword(trustStorePwd);
            }
            if (StringUtils.hasText(keyStore)) {
                factory.setKeyStore(keyStore);
            }
            if (StringUtils.hasText(keyStorePwd)) {
                factory.setKeyStorePassword(keyStorePwd);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure SSL for ActiveMQ", e);
        }
    }

    private void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                // ignore
            }
        }
    }
}
