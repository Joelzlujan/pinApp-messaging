package com.pinapp.messaging.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class NotificationResult {

    private final String notificationId;
    private final NotificationStatus status;
    private final String providerMessageId;
    private final String providerName;
    private final String errorMessage;
    private final Instant timestamp;

    public boolean isSuccess() {
        return status == NotificationStatus.SUCCESS;
    }
}
