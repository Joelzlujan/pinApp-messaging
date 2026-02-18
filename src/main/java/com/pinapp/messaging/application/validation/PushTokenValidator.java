package com.pinapp.messaging.application.validation;

import com.pinapp.messaging.application.exception.ValidationException;
import com.pinapp.messaging.domain.push.PushNotification;

public class PushTokenValidator implements NotificationValidator<PushNotification> {

    private static final int MIN_TOKEN_LENGTH = 10;

    @Override
    public void validate(PushNotification notification) {
        if (notification == null) {
            throw new ValidationException("Notification cannot be null");
        }
        if (notification.getRecipient() == null ||
                notification.getRecipient().getDeviceToken() == null ||
                notification.getRecipient().getDeviceToken().length() < MIN_TOKEN_LENGTH) {
            throw new ValidationException("Invalid device token");
        }
        if (notification.getTitle() == null || notification.getTitle().isBlank()) {
            throw new ValidationException("Push notification title is required");
        }
        if (notification.getBody() == null || notification.getBody().isBlank()) {
            throw new ValidationException("Push notification body is required");
        }
    }

    @Override
    public Class<PushNotification> getNotificationType() {
        return PushNotification.class;
    }
}
