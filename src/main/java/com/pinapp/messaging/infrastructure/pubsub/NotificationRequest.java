package com.pinapp.messaging.infrastructure.pubsub;

import com.pinapp.messaging.domain.Notification;
import com.pinapp.messaging.domain.Recipient;
import com.pinapp.messaging.domain.email.EmailNotification;
import com.pinapp.messaging.domain.push.PushNotification;
import com.pinapp.messaging.domain.sms.SmsNotification;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class NotificationRequest {

    private String id;
    private String type;
    private String email;
    private String phoneNumber;
    private String deviceToken;
    private String subject;
    private String title;
    private String body;
    private Map<String, Object> data;

    public Notification toNotification() {
        String notificationId = id != null ? id : UUID.randomUUID().toString();

        return switch (type.toUpperCase()) {
            case "EMAIL" -> EmailNotification.builder()
                    .id(notificationId)
                    .recipient(Recipient.builder().email(email).build())
                    .subject(subject)
                    .body(body)
                    .build();
            case "SMS" -> SmsNotification.builder()
                    .id(notificationId)
                    .recipient(Recipient.builder().phoneNumber(phoneNumber).build())
                    .body(body)
                    .build();
            case "PUSH" -> PushNotification.builder()
                    .id(notificationId)
                    .recipient(Recipient.builder().deviceToken(deviceToken).build())
                    .title(title)
                    .body(body)
                    .data(data != null ? data : Map.of())
                    .build();
            default -> throw new IllegalArgumentException("Unknown notification type: " + type);
        };
    }
}
