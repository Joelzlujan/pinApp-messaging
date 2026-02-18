package com.pinapp.messaging.infrastructure.provider.email;

import com.pinapp.messaging.application.port.NotificationSender;
import com.pinapp.messaging.domain.NotificationResult;
import com.pinapp.messaging.domain.NotificationStatus;
import com.pinapp.messaging.domain.email.EmailNotification;
import com.pinapp.messaging.infrastructure.config.ProviderCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

public class SendGridEmailSender implements NotificationSender<EmailNotification> {

    private static final Logger log = LoggerFactory.getLogger(SendGridEmailSender.class);
    private static final String PROVIDER_NAME = "SendGrid";

    private final ProviderCredentials credentials;

    public SendGridEmailSender(ProviderCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public NotificationResult send(EmailNotification notification) {
        log.info("[{}] Sending email to: {} | Subject: {}",
                PROVIDER_NAME,
                notification.getRecipient().getEmail(),
                notification.getSubject()
        );

        String providerMessageId = "sg-" + UUID.randomUUID();

        log.info("[{}] Email sent successfully. Provider ID: {}",
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
    public Class<EmailNotification> getNotificationType() {
        return EmailNotification.class;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
}
