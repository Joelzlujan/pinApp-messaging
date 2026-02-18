package com.pinapp.messaging.infrastructure.provider.push;

import com.pinapp.messaging.domain.NotificationResult;
import com.pinapp.messaging.domain.NotificationStatus;
import com.pinapp.messaging.domain.Recipient;
import com.pinapp.messaging.domain.push.PushNotification;
import com.pinapp.messaging.infrastructure.config.ProviderCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FirebasePushSenderTest {

    private FirebasePushSender sender;

    @BeforeEach
    void setUp() {
        sender = new FirebasePushSender(
                ProviderCredentials.builder()
                        .projectId("my-firebase-project")
                        .serviceAccountKey("/path/to/key.json")
                        .build()
        );
    }

    @Test
    void shouldSendPushSuccessfully() {
        PushNotification push = PushNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().deviceToken("fcm-device-token-xxx").build())
                .title("New message")
                .body("You have a new message from John")
                .data(Map.of("messageId", "12345", "action", "OPEN_CHAT"))
                .build();

        NotificationResult result = sender.send(push);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
        assertThat(result.getProviderName()).isEqualTo("Firebase");
        assertThat(result.getProviderMessageId()).contains("projects/my-firebase-project/messages/");
        assertThat(result.getNotificationId()).isEqualTo(push.getId());
        assertThat(result.getTimestamp()).isNotNull();
    }

    @Test
    void shouldReturnCorrectProviderName() {
        assertThat(sender.getProviderName()).isEqualTo("Firebase");
    }

    @Test
    void shouldReturnCorrectNotificationType() {
        assertThat(sender.getNotificationType()).isEqualTo(PushNotification.class);
    }
}
