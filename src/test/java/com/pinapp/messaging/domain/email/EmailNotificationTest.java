package com.pinapp.messaging.domain.email;

import com.pinapp.messaging.domain.Recipient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNotificationTest {

    @Test
    void shouldCreateEmailNotificationWithAllFields() {
        Recipient recipient = Recipient.builder()
                .email("test@example.com")
                .name("John Doe")
                .build();

        EmailNotification email = EmailNotification.builder()
                .id("email-123")
                .recipient(recipient)
                .subject("Test Subject")
                .body("Test Body")
                .htmlBody("<p>Test Body</p>")
                .fromEmail("sender@example.com")
                .fromName("Sender Name")
                .cc(List.of("cc1@example.com", "cc2@example.com"))
                .bcc(List.of("bcc@example.com"))
                .metadata(Map.of("key", "value"))
                .build();

        assertThat(email.getId()).isEqualTo("email-123");
        assertThat(email.getRecipient().getEmail()).isEqualTo("test@example.com");
        assertThat(email.getSubject()).isEqualTo("Test Subject");
        assertThat(email.getBody()).isEqualTo("Test Body");
        assertThat(email.getHtmlBody()).isEqualTo("<p>Test Body</p>");
        assertThat(email.getFromEmail()).isEqualTo("sender@example.com");
        assertThat(email.getFromName()).isEqualTo("Sender Name");
        assertThat(email.getCc()).hasSize(2);
        assertThat(email.getBcc()).hasSize(1);
        assertThat(email.getMetadata()).containsEntry("key", "value");
    }

    @Test
    void shouldCreateEmailNotificationWithDefaultMetadata() {
        EmailNotification email = EmailNotification.builder()
                .id("email-456")
                .recipient(Recipient.builder().email("test@example.com").build())
                .subject("Subject")
                .body("Body")
                .build();

        assertThat(email.getMetadata()).isNotNull();
        assertThat(email.getMetadata()).isEmpty();
    }
}
