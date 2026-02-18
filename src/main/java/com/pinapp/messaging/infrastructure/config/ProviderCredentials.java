package com.pinapp.messaging.infrastructure.config;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProviderCredentials {

    private final String apiKey;
    private final String apiSecret;
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final String fromEmail;
    private final String projectId;
    private final String serviceAccountKey;
}
