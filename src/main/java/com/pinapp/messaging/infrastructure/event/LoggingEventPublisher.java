package com.pinapp.messaging.infrastructure.event;

import com.pinapp.messaging.application.event.NotificationEvent;
import com.pinapp.messaging.application.port.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventPublisher.class);

    @Override
    public void publish(NotificationEvent event) {
        log.info("[EVENT] {} | notification={} | type={} | attempt={} | provider={} | error={}",
                event.getEventType(),
                event.getNotificationId(),
                event.getNotificationType(),
                event.getAttemptNumber(),
                event.getProviderName(),
                event.getErrorMessage()
        );
    }
}
