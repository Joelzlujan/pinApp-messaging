package com.pinapp.messaging.infrastructure.pubsub;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.gson.Gson;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.pinapp.messaging.MessagingClient;
import com.pinapp.messaging.application.port.MessageSubscriber;
import com.pinapp.messaging.domain.Notification;
import com.pinapp.messaging.domain.NotificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PubSubMessageSubscriber implements MessageSubscriber {

    private static final Logger log = LoggerFactory.getLogger(PubSubMessageSubscriber.class);

    private final PubSubConfig config;
    private final MessagingClient client;
    private final Gson gson;
    private Subscriber subscriber;
    private volatile boolean running = false;

    public PubSubMessageSubscriber(PubSubConfig config, MessagingClient client) {
        this.config = config;
        this.client = client;
        this.gson = new Gson();
    }

    @Override
    public void start() {
        if (running) {
            log.warn("Subscriber already running");
            return;
        }

        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(
                config.getProjectId(),
                config.getRequestSubscription()
        );

        MessageReceiver receiver = this::handleMessage;

        subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
        subscriber.startAsync().awaitRunning();
        running = true;

        log.info("PubSub subscriber started for subscription: {}", config.getRequestSubscription());
    }

    private void handleMessage(PubsubMessage message, AckReplyConsumer consumer) {
        try {
            String json = message.getData().toStringUtf8();
            log.debug("Received message: {}", json);

            NotificationRequest request = gson.fromJson(json, NotificationRequest.class);
            Notification notification = request.toNotification();

            NotificationResult result = client.send(notification);

            if (result.isSuccess()) {
                consumer.ack();
                log.info("Message processed successfully: {}", notification.getId());
            } else {
                consumer.nack();
                log.warn("Message processing failed: {} - {}", notification.getId(), result.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Error processing message", e);
            consumer.nack();
        }
    }

    @Override
    public void stop() {
        if (subscriber != null) {
            subscriber.stopAsync();
            try {
                subscriber.awaitTerminated(30, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.warn("Timeout waiting for subscriber to terminate");
            }
        }
        running = false;
        log.info("PubSub subscriber stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
