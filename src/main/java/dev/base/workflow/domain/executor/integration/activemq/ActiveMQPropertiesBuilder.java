package dev.base.workflow.domain.executor.integration.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

import static dev.base.workflow.constant.ActiveMQConstants.*;

@Component
public class ActiveMQPropertiesBuilder {

    public ActiveMQConnectionFactory buildConnectionFactory(Map<String, Object> config) {
        try {
            ActiveMQConnectionFactory factory;
            if (isSslEnabled(config)) {
                factory = createSslConnectionFactory(config);
            } else {
                factory = new ActiveMQConnectionFactory();
                String brokerUrl = (String) config.get(CFG_BROKER_URL);
                if (brokerUrl != null && !brokerUrl.isEmpty()) {
                    factory.setBrokerURL(brokerUrl);
                }
            }

            configureCredentials(factory, config);
            return factory;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build ActiveMQ Connection Factory", e);
        }
    }

    private boolean isSslEnabled(Map<String, Object> config) {
        return Boolean.TRUE.equals(config.get(CFG_SSL_ENABLED));
    }

    private ActiveMQConnectionFactory createSslConnectionFactory(Map<String, Object> config) throws Exception {
        String brokerUrl = (String) config.get(CFG_BROKER_URL);
        ActiveMQSslConnectionFactory sslFactory = new ActiveMQSslConnectionFactory(brokerUrl);
        configureSslProperties(sslFactory, config);
        return sslFactory;
    }

    private void configureCredentials(ActiveMQConnectionFactory factory, Map<String, Object> config) {
        String username = (String) config.get(CFG_USERNAME);
        String password = (String) config.get(CFG_PASSWORD);

        if (username != null && !username.isEmpty()) {
            factory.setUserName(username);
        }
        if (password != null && !password.isEmpty()) {
            factory.setPassword(password);
        }
    }

    private void configureSslProperties(ActiveMQSslConnectionFactory factory, Map<String, Object> config)
            throws Exception {
        String trustStore = (String) config.get(CFG_SSL_TRUSTSTORE_LOC);
        String trustStorePwd = (String) config.get(CFG_SSL_TRUSTSTORE_PWD);
        String keyStore = (String) config.get(CFG_SSL_KEYSTORE_LOC);
        String keyStorePwd = (String) config.get(CFG_SSL_KEYSTORE_PWD);

        if (trustStore != null && !trustStore.isEmpty())
            factory.setTrustStore(trustStore);
        if (trustStorePwd != null && !trustStorePwd.isEmpty())
            factory.setTrustStorePassword(trustStorePwd);
        if (keyStore != null && !keyStore.isEmpty())
            factory.setKeyStore(keyStore);
        if (keyStorePwd != null && !keyStorePwd.isEmpty())
            factory.setKeyStorePassword(keyStorePwd);
    }
}
