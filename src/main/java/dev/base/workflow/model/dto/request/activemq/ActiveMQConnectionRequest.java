package dev.base.workflow.model.dto.request.activemq;

import lombok.Data;

@Data
public class ActiveMQConnectionRequest {
    private String brokerUrl;
    private String username;
    private String password;

    // SSL Config
    private boolean sslEnabled;
    private String sslTrustStoreLocation;
    private String sslTrustStorePassword;
    private String sslKeyStoreLocation;
    private String sslKeyStorePassword;

    // Filter for destinations
    private String destinationType;
}
