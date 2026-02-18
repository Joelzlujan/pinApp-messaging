package com.pinapp.messaging.infrastructure.provider.email;

import com.pinapp.messaging.domain.NotificationResult;
import com.pinapp.messaging.domain.NotificationStatus;
import com.pinapp.messaging.domain.Recipient;
import com.pinapp.messaging.domain.email.EmailNotification;
import com.pinapp.messaging.infrastructure.config.ProviderCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MailgunEmailSenderTest {

    private MailgunEmailSender sender;

    @BeforeEach
    void setUp() {
        sender = new MailgunEmailSender(
                ProviderCredentials.builder()
                        .apiKey("test-api-key")
                        .build()
        );
    }

    @Test
    void shouldSendEmailSuccessfully() {
        EmailNotification email = EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().email("test@example.com").build())
                .subject("Test Subject")
                .body("Test Body")
                .build();

        NotificationResult result = sender.send(email);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
        assertThat(result.getProviderName()).isEqualTo("Mailgun");
        assertThat(result.getProviderMessageId()).startsWith("mg-");
        assertThat(result.getNotificationId()).isEqualTo(email.getId());
    }

    @Test
    void shouldReturnCorrectProviderName() {
        assertThat(sender.getProviderName()).isEqualTo("Mailgun");
    }

    @Test
    void shouldReturnCorrectNotificationType() {
        assertThat(sender.getNotificationType()).isEqualTo(EmailNotification.class);
    }
}
