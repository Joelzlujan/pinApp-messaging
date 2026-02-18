package com.pinapp.messaging.domain.push;

import com.pinapp.messaging.domain.Notification;
import com.pinapp.messaging.domain.Recipient;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class PushNotification implements Notification {

    private final String id;
    private final Recipient recipient;
    private final String title;
    private final String body;

    @Builder.Default
    private final Map<String, Object> data = Map.of();

    @Builder.Default
    private final Map<String, Object> metadata = Map.of();
}
