package com.pinapp.messaging.application.port;

import com.pinapp.messaging.domain.Notification;
import com.pinapp.messaging.domain.NotificationResult;

public interface NotificationSender<T extends Notification> {

    NotificationResult send(T notification);

    Class<T> getNotificationType();

    String getProviderName();
}
