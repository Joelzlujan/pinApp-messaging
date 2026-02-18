package com.pinapp.messaging.application.validation;

import com.pinapp.messaging.application.exception.ValidationException;
import com.pinapp.messaging.domain.email.EmailNotification;

import java.util.regex.Pattern;

public class EmailValidator implements NotificationValidator<EmailNotification> {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public void validate(EmailNotification notification) {
        if (notification == null) {
            throw new ValidationException("Notification cannot be null");
        }
        if (notification.getRecipient() == null ||
                notification.getRecipient().getEmail() == null ||
                !EMAIL_PATTERN.matcher(notification.getRecipient().getEmail()).matches()) {
            throw new ValidationException("Invalid email address");
        }
        if (notification.getSubject() == null || notification.getSubject().isBlank()) {
            throw new ValidationException("Email subject is required");
        }
        if (notification.getBody() == null || notification.getBody().isBlank()) {
            throw new ValidationException("Email body is required");
        }
    }

    @Override
    public Class<EmailNotification> getNotificationType() {
        return EmailNotification.class;
    }
}
