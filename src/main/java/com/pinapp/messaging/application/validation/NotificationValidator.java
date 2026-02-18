package com.pinapp.messaging.application.validation;

import com.pinapp.messaging.domain.Notification;

public interface NotificationValidator<T extends Notification> {

    void validate(T notification);

    Class<T> getNotificationType();
}
