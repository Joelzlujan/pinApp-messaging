package com.pinapp.messaging.infrastructure.pubsub;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PubSubConfig {

    private final String projectId;
    private final String statusTopic;
    private final String requestSubscription;

    public static PubSubConfig of(String projectId, String statusTopic, String requestSubscription) {
        return PubSubConfig.builder()
                .projectId(projectId)
                .statusTopic(statusTopic)
                .requestSubscription(requestSubscription)
                .build();
    }
}
