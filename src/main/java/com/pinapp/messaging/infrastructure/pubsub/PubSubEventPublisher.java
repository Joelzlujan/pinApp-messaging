package com.pinapp.messaging.infrastructure.pubsub;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import com.pinapp.messaging.application.event.NotificationEvent;
import com.pinapp.messaging.application.port.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class PubSubEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PubSubEventPublisher.class);

    private final PubSubConfig config;
    private final Gson gson;
    private Publisher publisher;

    public PubSubEventPublisher(PubSubConfig config) {
        this.config = config;
        this.gson = new Gson();
        initPublisher();
    }

    private void initPublisher() {
        try {
            TopicName topicName = TopicName.of(config.getProjectId(), config.getStatusTopic());
            this.publisher = Publisher.newBuilder(topicName).build();
            log.info("PubSub publisher initialized for topic: {}", config.getStatusTopic());
        } catch (Exception e) {
            log.error("Failed to initialize PubSub publisher", e);
            throw new RuntimeException("Failed to initialize PubSub publisher", e);
        }
    }

    @Override
    public void publish(NotificationEvent event) {
        try {
            String json = gson.toJson(event);
            PubsubMessage message = PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8(json))
                    .putAttributes("eventType", event.getEventType().name())
                    .putAttributes("notificationId", event.getNotificationId())
                    .putAttributes("notificationType", event.getNotificationType())
                    .build();

            publisher.publish(message).get();
            log.debug("Published event: {} for notification: {}",
                    event.getEventType(), event.getNotificationId());
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event, e);
        }
    }

    public void shutdown() {
        if (publisher != null) {
            try {
                publisher.shutdown();
                publisher.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
