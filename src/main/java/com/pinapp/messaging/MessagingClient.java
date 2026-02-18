package com.pinapp.messaging;

import com.pinapp.messaging.application.port.EventPublisher;
import com.pinapp.messaging.application.port.NotificationSender;
import com.pinapp.messaging.application.retry.RetryPolicy;
import com.pinapp.messaging.application.service.send.SendNotificationService;
import com.pinapp.messaging.application.service.send.usecase.SendNotificationUseCase;
import com.pinapp.messaging.application.validation.EmailValidator;
import com.pinapp.messaging.application.validation.NotificationValidator;
import com.pinapp.messaging.application.validation.PhoneValidator;
import com.pinapp.messaging.application.validation.PushTokenValidator;
import com.pinapp.messaging.domain.Notification;
import com.pinapp.messaging.domain.NotificationResult;

import java.util.concurrent.CompletableFuture;
import com.pinapp.messaging.domain.email.EmailNotification;
import com.pinapp.messaging.domain.push.PushNotification;
import com.pinapp.messaging.domain.sms.SmsNotification;

public class MessagingClient {

    private final SendNotificationUseCase sendNotificationUseCase;

    private MessagingClient(SendNotificationUseCase sendNotificationUseCase) {
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    public <T extends Notification> NotificationResult send(T notification) {
        return sendNotificationUseCase.send(notification);
    }

    public <T extends Notification> CompletableFuture<NotificationResult> sendAsync(T notification) {
        return sendNotificationUseCase.sendAsync(notification);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private NotificationSender<EmailNotification> emailSender;
        private NotificationSender<SmsNotification> smsSender;
        private NotificationSender<PushNotification> pushSender;
        private boolean validationEnabled = true;
        private RetryPolicy retryPolicy;
        private EventPublisher eventPublisher;

        public Builder withEmailSender(NotificationSender<EmailNotification> sender) {
            this.emailSender = sender;
            return this;
        }

        public Builder withSmsSender(NotificationSender<SmsNotification> sender) {
            this.smsSender = sender;
            return this;
        }

        public Builder withPushSender(NotificationSender<PushNotification> sender) {
            this.pushSender = sender;
            return this;
        }

        public Builder withValidation(boolean enabled) {
            this.validationEnabled = enabled;
            return this;
        }

        public Builder withRetryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        public Builder withEventPublisher(EventPublisher eventPublisher) {
            this.eventPublisher = eventPublisher;
            return this;
        }

        public MessagingClient build() {
            NotificationValidator<EmailNotification> emailValidator = null;
            NotificationValidator<SmsNotification> smsValidator = null;
            NotificationValidator<PushNotification> pushValidator = null;

            if (validationEnabled) {
                emailValidator = new EmailValidator();
                smsValidator = new PhoneValidator();
                pushValidator = new PushTokenValidator();
            }

            SendNotificationService service = new SendNotificationService(
                    emailSender,
                    smsSender,
                    pushSender,
                    emailValidator,
                    smsValidator,
                    pushValidator,
                    validationEnabled,
                    retryPolicy,
                    eventPublisher
            );

            return new MessagingClient(service);
        }
    }
}
