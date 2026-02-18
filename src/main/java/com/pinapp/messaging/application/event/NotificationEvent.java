package com.pinapp.messaging.application.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class NotificationEvent {

    private final EventType eventType;
    private final String notificationId;
    private final String notificationType;
    private final int attemptNumber;
    private final String errorMessage;
    private final String providerName;
    private final String providerMessageId;

    @Builder.Default
    private final Instant timestamp = Instant.now();
}
