package com.pinapp.messaging.infrastructure.provider.sms;

import com.pinapp.messaging.domain.NotificationResult;
import com.pinapp.messaging.domain.NotificationStatus;
import com.pinapp.messaging.domain.Recipient;
import com.pinapp.messaging.domain.sms.SmsNotification;
import com.pinapp.messaging.infrastructure.config.ProviderCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TwilioSmsSenderTest {

    private TwilioSmsSender sender;

    @BeforeEach
    void setUp() {
        sender = new TwilioSmsSender(
                ProviderCredentials.builder()
                        .accountSid("test-account-sid")
                        .authToken("test-auth-token")
                        .fromNumber("+1234567890")
                        .build()
        );
    }

    @Test
    void shouldSendSmsSuccessfully() {
        SmsNotification sms = SmsNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().phoneNumber("+5491155551234").build())
                .body("Your verification code is: 123456")
                .build();

        NotificationResult result = sender.send(sms);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
        assertThat(result.getProviderName()).isEqualTo("Twilio");
        assertThat(result.getProviderMessageId()).startsWith("SM");
        assertThat(result.getNotificationId()).isEqualTo(sms.getId());
        assertThat(result.getTimestamp()).isNotNull();
    }

    @Test
    void shouldReturnCorrectProviderName() {
        assertThat(sender.getProviderName()).isEqualTo("Twilio");
    }

    @Test
    void shouldReturnCorrectNotificationType() {
        assertThat(sender.getNotificationType()).isEqualTo(SmsNotification.class);
    }
}
