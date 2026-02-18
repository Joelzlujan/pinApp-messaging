package com.pinapp.messaging.domain.sms;

import com.pinapp.messaging.domain.Notification;
import com.pinapp.messaging.domain.Recipient;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class SmsNotification implements Notification {

    private final String id;
    private final Recipient recipient;
    private final String body;
    private final String fromNumber;

    @Builder.Default
    private final Map<String, Object> metadata = Map.of();
}
