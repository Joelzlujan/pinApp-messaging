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

class SendGridEmailSenderTest {

    private SendGridEmailSender sender;

    @BeforeEach
    void setUp() {
        sender = new SendGridEmailSender(
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
        assertThat(result.getProviderName()).isEqualTo("SendGrid");
        assertThat(result.getProviderMessageId()).startsWith("sg-");
        assertThat(result.getNotificationId()).isEqualTo(email.getId());
        assertThat(result.getTimestamp()).isNotNull();
    }

    @Test
    void shouldReturnCorrectProviderName() {
        assertThat(sender.getProviderName()).isEqualTo("SendGrid");
    }

    @Test
    void shouldReturnCorrectNotificationType() {
        assertThat(sender.getNotificationType()).isEqualTo(EmailNotification.class);
    }
}
