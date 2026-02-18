package com.pinapp.messaging.infrastructure.provider.sms;

import com.pinapp.messaging.application.port.NotificationSender;
import com.pinapp.messaging.domain.NotificationResult;
import com.pinapp.messaging.domain.NotificationStatus;
import com.pinapp.messaging.domain.sms.SmsNotification;
import com.pinapp.messaging.infrastructure.config.ProviderCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

public class TwilioSmsSender implements NotificationSender<SmsNotification> {

    private static final Logger log = LoggerFactory.getLogger(TwilioSmsSender.class);
    private static final String PROVIDER_NAME = "Twilio";

    private final ProviderCredentials credentials;

    public TwilioSmsSender(ProviderCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public NotificationResult send(SmsNotification notification) {
        String bodyPreview = notification.getBody().substring(
                0, Math.min(50, notification.getBody().length())
        ) + "...";

        log.info("[{}] Sending SMS to: {} | Body: {}",
                PROVIDER_NAME,
                notification.getRecipient().getPhoneNumber(),
                bodyPreview
        );

        String providerMessageId = "SM" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);

        log.info("[{}] SMS sent successfully. SID: {}", PROVIDER_NAME, providerMessageId);

        return NotificationResult.builder()
                .notificationId(notification.getId())
                .status(NotificationStatus.SUCCESS)
                .providerMessageId(providerMessageId)
                .providerName(PROVIDER_NAME)
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public Class<SmsNotification> getNotificationType() {
        return SmsNotification.class;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
}
