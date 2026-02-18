package com.pinapp.messaging.application.port;

import com.pinapp.messaging.application.event.NotificationEvent;

public interface EventPublisher {

    void publish(NotificationEvent event);
}
