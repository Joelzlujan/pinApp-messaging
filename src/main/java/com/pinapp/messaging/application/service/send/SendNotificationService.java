package com.pinapp.messaging.application.service.send;

import com.pinapp.messaging.application.event.EventType;
import com.pinapp.messaging.application.event.NotificationEvent;
import com.pinapp.messaging.application.exception.ConfigurationException;
import com.pinapp.messaging.application.port.EventPublisher;
import com.pinapp.messaging.application.port.NotificationSender;
import com.pinapp.messaging.application.retry.RetryPolicy;
import com.pinapp.messaging.application.service.send.usecase.SendNotificationUseCase;
import com.pinapp.messaging.application.validation.NotificationValidator;
import com.pinapp.messaging.domain.Notification;
import com.pinapp.messaging.domain.NotificationResult;
import com.pinapp.messaging.domain.NotificationStatus;
import com.pinapp.messaging.domain.email.EmailNotification;
import com.pinapp.messaging.domain.push.PushNotification;
import com.pinapp.messaging.domain.sms.SmsNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendNotificationService implements SendNotificationUseCase {

    private static final ExecutorService VIRTUAL_EXECUTOR =
            Executors.newVirtualThreadPerTaskExecutor();

    private static final Logger log = LoggerFactory.getLogger(SendNotificationService.class);

    private final NotificationSender<EmailNotification> emailSender;
    private final NotificationSender<SmsNotification> smsSender;
    private final NotificationSender<PushNotification> pushSender;

    private final NotificationValidator<EmailNotification> emailValidator;
    private final NotificationValidator<SmsNotification> smsValidator;
    private final NotificationValidator<PushNotification> pushValidator;

    private final boolean validationEnabled;
    private final RetryPolicy retryPolicy;
    private final EventPublisher eventPublisher;

    public SendNotificationService(
            NotificationSender<EmailNotification> emailSender,
            NotificationSender<SmsNotification> smsSender,
            NotificationSender<PushNotification> pushSender,
            NotificationValidator<EmailNotification> emailValidator,
            NotificationValidator<SmsNotification> smsValidator,
            NotificationValidator<PushNotification> pushValidator,
            boolean validationEnabled,
            RetryPolicy retryPolicy,
            EventPublisher eventPublisher
    ) {
        this.emailSender = emailSender;
        this.smsSender = smsSender;
        this.pushSender = pushSender;
        this.emailValidator = emailValidator;
        this.smsValidator = smsValidator;
        this.pushValidator = pushValidator;
        this.validationEnabled = validationEnabled;
        this.retryPolicy = retryPolicy != null ? retryPolicy : RetryPolicy.none();
        this.eventPublisher = eventPublisher;
    }

    @Override
    public <T extends Notification> NotificationResult send(T notification) {
        if (notification instanceof EmailNotification email) {
            return sendWithRetry(email, emailSender, emailValidator, "EMAIL");
        }
        if (notification instanceof SmsNotification sms) {
            return sendWithRetry(sms, smsSender, smsValidator, "SMS");
        }
        if (notification instanceof PushNotification push) {
            return sendWithRetry(push, pushSender, pushValidator, "PUSH");
        }
        throw new ConfigurationException(
                "Unsupported notification type: " + notification.getClass().getSimpleName()
        );
    }

    @Override
    public <T extends Notification> CompletableFuture<NotificationResult> sendAsync(T notification) {
        return CompletableFuture.supplyAsync(() -> send(notification), VIRTUAL_EXECUTOR);
    }

    private <T extends Notification> NotificationResult sendWithRetry(
            T notification,
            NotificationSender<T> sender,
            NotificationValidator<T> validator,
            String typeName
    ) {
        if (sender == null) {
            throw new ConfigurationException("No " + typeName.toLowerCase() + " sender configured");
        }

        if (validationEnabled && validator != null) {
            validator.validate(notification);
        }

        publishEvent(EventType.SENDING, notification, typeName, 1, null, null, null);

        NotificationResult result = null;
        Exception lastException = null;

        for (int attempt = 1; attempt <= retryPolicy.getMaxAttempts(); attempt++) {
            try {
                log.info("Sending {} via {} (attempt {}/{})",
                        typeName, sender.getProviderName(), attempt, retryPolicy.getMaxAttempts());

                result = sender.send(notification);

                if (result.isSuccess()) {
                    publishEvent(EventType.SUCCESS, notification, typeName, attempt,
                            null, result.getProviderName(), result.getProviderMessageId());
                    return result;
                }

                lastException = new RuntimeException(result.getErrorMessage());

            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {}/{} failed: {}", attempt, retryPolicy.getMaxAttempts(), e.getMessage());
            }

            if (attempt < retryPolicy.getMaxAttempts()) {
                publishEvent(EventType.RETRYING, notification, typeName, attempt + 1,
                        lastException.getMessage(), null, null);
                sleep(retryPolicy.getDelayForAttempt(attempt));
            }
        }

        String errorMessage = lastException != null ? lastException.getMessage() : "Unknown error";
        publishEvent(EventType.FAILED, notification, typeName, retryPolicy.getMaxAttempts(),
                errorMessage, null, null);

        if (result != null) {
            return result;
        }

        return NotificationResult.builder()
                .notificationId(notification.getId())
                .status(NotificationStatus.FAILED)
                .errorMessage(errorMessage)
                .timestamp(Instant.now())
                .build();
    }

    private void publishEvent(
            EventType eventType,
            Notification notification,
            String notificationType,
            int attemptNumber,
            String errorMessage,
            String providerName,
            String providerMessageId
    ) {
        if (eventPublisher == null) {
            return;
        }

        NotificationEvent event = NotificationEvent.builder()
                .eventType(eventType)
                .notificationId(notification.getId())
                .notificationType(notificationType)
                .attemptNumber(attemptNumber)
                .errorMessage(errorMessage)
                .providerName(providerName)
                .providerMessageId(providerMessageId)
                .build();

        try {
            eventPublisher.publish(event);
        } catch (Exception e) {
            log.error("Failed to publish event: {}", e.getMessage());
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
