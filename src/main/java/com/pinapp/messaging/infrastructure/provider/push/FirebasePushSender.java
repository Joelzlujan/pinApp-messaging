package com.pinapp.messaging.infrastructure.provider.push;

import com.pinapp.messaging.application.port.NotificationSender;
import com.pinapp.messaging.domain.NotificationResult;
import com.pinapp.messaging.domain.NotificationStatus;
import com.pinapp.messaging.domain.push.PushNotification;
import com.pinapp.messaging.infrastructure.config.ProviderCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

public class FirebasePushSender implements NotificationSender<PushNotification> {

    private static final Logger log = LoggerFactory.getLogger(FirebasePushSender.class);
    private static final String PROVIDER_NAME = "Firebase";

    private final ProviderCredentials credentials;

    public FirebasePushSender(ProviderCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public NotificationResult send(PushNotification notification) {
        log.info("[{}] Sending push to device: {} | Title: {}",
                PROVIDER_NAME,
                notification.getRecipient().getDeviceToken(),
                notification.getTitle()
        );

        String providerMessageId = "projects/" + credentials.getProjectId() +
                "/messages/" + UUID.randomUUID();

        log.info("[{}] Push sent successfully. Message ID: {}",
                PROVIDER_NAME, providerMessageId);

        return NotificationResult.builder()
                .notificationId(notification.getId())
                .status(NotificationStatus.SUCCESS)
                .providerMessageId(providerMessageId)
                .providerName(PROVIDER_NAME)
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public Class<PushNotification> getNotificationType() {
        return PushNotification.class;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
}
