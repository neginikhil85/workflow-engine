package dev.base.workflow.model.dto.request.kafka;

import lombok.Data;

@Data
public class KafkaConnectionRequest {
    private String bootstrapServers;
    private String securityProtocol;
    private String saslMechanism;
    private String saslJaasConfig;

    // SSL Config
    private String sslTrustStoreLocation;
    private String sslTrustStorePassword;
    private String sslKeyStoreLocation;
    private String sslKeyStorePassword;
}
