package com.pinapp.messaging;

import com.pinapp.messaging.application.exception.ConfigurationException;
import com.pinapp.messaging.application.exception.ValidationException;
import com.pinapp.messaging.domain.NotificationResult;
import com.pinapp.messaging.domain.Recipient;
import com.pinapp.messaging.domain.email.EmailNotification;
import com.pinapp.messaging.domain.push.PushNotification;
import com.pinapp.messaging.domain.sms.SmsNotification;
import com.pinapp.messaging.infrastructure.config.ProviderCredentials;
import com.pinapp.messaging.infrastructure.provider.email.SendGridEmailSender;
import com.pinapp.messaging.infrastructure.provider.push.FirebasePushSender;
import com.pinapp.messaging.infrastructure.provider.sms.TwilioSmsSender;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessagingClientTest {

    @Test
    void shouldSendEmailWhenProviderConfigured() {
        MessagingClient client = MessagingClient.builder()
                .withEmailSender(new SendGridEmailSender(
                        ProviderCredentials.builder().apiKey("test").build()
                ))
                .build();

        EmailNotification email = EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().email("test@example.com").build())
                .subject("Test")
                .body("Body")
                .build();

        NotificationResult result = client.send(email);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProviderName()).isEqualTo("SendGrid");
    }

    @Test
    void shouldSendSmsWhenProviderConfigured() {
        MessagingClient client = MessagingClient.builder()
                .withSmsSender(new TwilioSmsSender(
                        ProviderCredentials.builder()
                                .accountSid("test-sid")
                                .authToken("test-token")
                                .build()
                ))
                .build();

        SmsNotification sms = SmsNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().phoneNumber("+5491155551234").build())
                .body("Test SMS")
                .build();

        NotificationResult result = client.send(sms);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProviderName()).isEqualTo("Twilio");
    }

    @Test
    void shouldSendPushWhenProviderConfigured() {
        MessagingClient client = MessagingClient.builder()
                .withPushSender(new FirebasePushSender(
                        ProviderCredentials.builder()
                                .projectId("test-project")
                                .build()
                ))
                .build();

        PushNotification push = PushNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().deviceToken("valid-device-token-12345").build())
                .title("Test Title")
                .body("Test Body")
                .data(Map.of("key", "value"))
                .build();

        NotificationResult result = client.send(push);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProviderName()).isEqualTo("Firebase");
    }

    @Test
    void shouldThrowWhenNoProviderConfigured() {
        MessagingClient client = MessagingClient.builder().build();

        EmailNotification email = EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().email("test@example.com").build())
                .subject("Test")
                .body("Body")
                .build();

        assertThatThrownBy(() -> client.send(email))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("No email sender configured");
    }

    @Test
    void shouldValidateBeforeSending() {
        MessagingClient client = MessagingClient.builder()
                .withEmailSender(new SendGridEmailSender(
                        ProviderCredentials.builder().apiKey("test").build()
                ))
                .withValidation(true)
                .build();

        EmailNotification invalidEmail = EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().email("invalid").build())
                .subject("Test")
                .body("Body")
                .build();

        assertThatThrownBy(() -> client.send(invalidEmail))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email");
    }

    @Test
    void shouldSkipValidationWhenDisabled() {
        MessagingClient client = MessagingClient.builder()
                .withEmailSender(new SendGridEmailSender(
                        ProviderCredentials.builder().apiKey("test").build()
                ))
                .withValidation(false)
                .build();

        EmailNotification invalidEmail = EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().email("invalid").build())
                .subject("Test")
                .body("Body")
                .build();

        NotificationResult result = client.send(invalidEmail);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void shouldSupportMultipleProviders() {
        MessagingClient client = MessagingClient.builder()
                .withEmailSender(new SendGridEmailSender(
                        ProviderCredentials.builder().apiKey("test").build()
                ))
                .withSmsSender(new TwilioSmsSender(
                        ProviderCredentials.builder()
                                .accountSid("test-sid")
                                .authToken("test-token")
                                .build()
                ))
                .withPushSender(new FirebasePushSender(
                        ProviderCredentials.builder()
                                .projectId("test-project")
                                .build()
                ))
                .build();

        EmailNotification email = EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().email("test@example.com").build())
                .subject("Test")
                .body("Body")
                .build();

        SmsNotification sms = SmsNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().phoneNumber("+5491155551234").build())
                .body("Test SMS")
                .build();

        PushNotification push = PushNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().deviceToken("valid-device-token-12345").build())
                .title("Test Title")
                .body("Test Body")
                .build();

        assertThat(client.send(email).isSuccess()).isTrue();
        assertThat(client.send(sms).isSuccess()).isTrue();
        assertThat(client.send(push).isSuccess()).isTrue();
    }

    @Test
    void shouldSendAsyncAndGetResult() throws Exception {
        MessagingClient client = MessagingClient.builder()
                .withEmailSender(new SendGridEmailSender(
                        ProviderCredentials.builder().apiKey("test").build()
                ))
                .build();

        EmailNotification email = EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().email("test@example.com").build())
                .subject("Test")
                .body("Body")
                .build();

        CompletableFuture<NotificationResult> future = client.sendAsync(email);

        NotificationResult result = future.get();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProviderName()).isEqualTo("SendGrid");
    }

    @Test
    void shouldSendMultipleAsyncInParallel() throws Exception {
        MessagingClient client = MessagingClient.builder()
                .withEmailSender(new SendGridEmailSender(
                        ProviderCredentials.builder().apiKey("test").build()
                ))
                .build();

        List<CompletableFuture<NotificationResult>> futures = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            EmailNotification email = EmailNotification.builder()
                    .id(UUID.randomUUID().toString())
                    .recipient(Recipient.builder().email("test" + i + "@example.com").build())
                    .subject("Test " + i)
                    .body("Body " + i)
                    .build();

            futures.add(client.sendAsync(email));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (CompletableFuture<NotificationResult> future : futures) {
            assertThat(future.get().isSuccess()).isTrue();
        }
    }

    @Test
    void shouldHandleAsyncWithCallback() {
        MessagingClient client = MessagingClient.builder()
                .withEmailSender(new SendGridEmailSender(
                        ProviderCredentials.builder().apiKey("test").build()
                ))
                .build();

        EmailNotification email = EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().email("test@example.com").build())
                .subject("Test")
                .body("Body")
                .build();

        List<String> results = new ArrayList<>();

        client.sendAsync(email)
                .thenAccept(result -> results.add(result.getProviderName()))
                .join();

        assertThat(results).containsExactly("SendGrid");
    }
}
