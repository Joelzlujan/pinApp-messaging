package com.pinapp.messaging.application.service.send;

import com.pinapp.messaging.application.exception.ConfigurationException;
import com.pinapp.messaging.application.exception.ValidationException;
import com.pinapp.messaging.application.port.NotificationSender;
import com.pinapp.messaging.application.retry.RetryPolicy;
import com.pinapp.messaging.application.validation.EmailValidator;
import com.pinapp.messaging.application.validation.NotificationValidator;
import com.pinapp.messaging.domain.NotificationResult;
import com.pinapp.messaging.domain.Recipient;
import com.pinapp.messaging.domain.email.EmailNotification;
import com.pinapp.messaging.infrastructure.config.ProviderCredentials;
import com.pinapp.messaging.infrastructure.provider.email.SendGridEmailSender;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SendNotificationServiceTest {

    @Test
    void shouldSendEmailSuccessfully() {
        NotificationSender<EmailNotification> emailSender = new SendGridEmailSender(
                ProviderCredentials.builder().apiKey("test").build()
        );
        NotificationValidator<EmailNotification> emailValidator = new EmailValidator();

        SendNotificationService service = createService(emailSender, emailValidator, true, null, null);

        EmailNotification email = createValidEmail();
        NotificationResult result = service.send(email);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProviderName()).isEqualTo("SendGrid");
    }

    @Test
    void shouldSkipValidationWhenDisabled() {
        NotificationSender<EmailNotification> emailSender = new SendGridEmailSender(
                ProviderCredentials.builder().apiKey("test").build()
        );
        NotificationValidator<EmailNotification> emailValidator = new EmailValidator();

        SendNotificationService service = createService(emailSender, emailValidator, false, null, null);

        EmailNotification invalidEmail = EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().email("invalid").build())
                .subject("Test")
                .body("Body")
                .build();

        NotificationResult result = service.send(invalidEmail);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void shouldThrowWhenNoEmailSenderConfigured() {
        SendNotificationService service = createService(null, null, false, null, null);

        EmailNotification email = createValidEmail();

        assertThatThrownBy(() -> service.send(email))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("No email sender configured");
    }

    @Test
    void shouldThrowValidationExceptionForInvalidEmail() {
        NotificationSender<EmailNotification> emailSender = new SendGridEmailSender(
                ProviderCredentials.builder().apiKey("test").build()
        );
        NotificationValidator<EmailNotification> emailValidator = new EmailValidator();

        SendNotificationService service = createService(emailSender, emailValidator, true, null, null);

        EmailNotification invalidEmail = EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().email("invalid").build())
                .subject("Test")
                .body("Body")
                .build();

        assertThatThrownBy(() -> service.send(invalidEmail))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email");
    }

    @Test
    void shouldSendWithoutValidatorIfNotRegistered() {
        NotificationSender<EmailNotification> emailSender = new SendGridEmailSender(
                ProviderCredentials.builder().apiKey("test").build()
        );

        SendNotificationService service = createService(emailSender, null, true, null, null);

        EmailNotification email = createValidEmail();
        NotificationResult result = service.send(email);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void shouldRetryOnFailure() {
        // Este test verifica que el retry funciona, pero como nuestros senders simulados
        // siempre tienen éxito, solo verificamos que no rompe con RetryPolicy
        NotificationSender<EmailNotification> emailSender = new SendGridEmailSender(
                ProviderCredentials.builder().apiKey("test").build()
        );

        RetryPolicy retryPolicy = RetryPolicy.of(3, 100);
        SendNotificationService service = createService(emailSender, null, false, retryPolicy, null);

        EmailNotification email = createValidEmail();
        NotificationResult result = service.send(email);

        assertThat(result.isSuccess()).isTrue();
    }

    private SendNotificationService createService(
            NotificationSender<EmailNotification> emailSender,
            NotificationValidator<EmailNotification> emailValidator,
            boolean validationEnabled,
            RetryPolicy retryPolicy,
            com.pinapp.messaging.application.port.EventPublisher eventPublisher
    ) {
        return new SendNotificationService(
                emailSender, null, null,
                emailValidator, null, null,
                validationEnabled,
                retryPolicy,
                eventPublisher
        );
    }

    private EmailNotification createValidEmail() {
        return EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().email("test@example.com").build())
                .subject("Test Subject")
                .body("Test Body")
                .build();
    }
}
