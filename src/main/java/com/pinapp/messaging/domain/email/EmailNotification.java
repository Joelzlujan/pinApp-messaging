package com.pinapp.messaging.domain.email;

import com.pinapp.messaging.domain.Notification;
import com.pinapp.messaging.domain.Recipient;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class EmailNotification implements Notification {

    private final String id;
    private final Recipient recipient;
    private final String subject;
    private final String body;
    private final String htmlBody;
    private final List<String> cc;
    private final List<String> bcc;
    private final String fromEmail;
    private final String fromName;

    @Builder.Default
    private final Map<String, Object> metadata = Map.of();
}
