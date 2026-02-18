package com.pinapp.messaging.application.validation;

import com.pinapp.messaging.application.exception.ValidationException;
import com.pinapp.messaging.domain.sms.SmsNotification;

import java.util.regex.Pattern;

public class PhoneValidator implements NotificationValidator<SmsNotification> {

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[1-9]\\d{6,14}$");

    @Override
    public void validate(SmsNotification notification) {
        if (notification == null) {
            throw new ValidationException("Notification cannot be null");
        }
        if (notification.getRecipient() == null ||
                notification.getRecipient().getPhoneNumber() == null ||
                !PHONE_PATTERN.matcher(notification.getRecipient().getPhoneNumber()).matches()) {
            throw new ValidationException("Invalid phone number");
        }
        if (notification.getBody() == null || notification.getBody().isBlank()) {
            throw new ValidationException("SMS body is required");
        }
    }

    @Override
    public Class<SmsNotification> getNotificationType() {
        return SmsNotification.class;
    }
}
