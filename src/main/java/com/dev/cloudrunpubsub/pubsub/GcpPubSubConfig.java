package dev.cloudrunpubsub.pubsub;

import com.google.api.gax.core.CredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GcpPubSubConfig {

    @Value("$pubsub.wif.client-id}")
    private String wifClientId;

    @Value("${pubsub.wif.client-secret}")
    private String wifClientSecret;

    @Value("${.pubsub.project-id}")
    private String gcpProjectId;

    @Value("${pubsub.service-account}")
    private String gcpServiceAccount;

    @Bean
    public CredentialsProvider googleCredentials() {
        return new GcpCredentialsProvider(wifClientId, wifClientSecret, gcpProjectId, gcpServiceAccount);
    }


}
