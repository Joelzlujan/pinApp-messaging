package com.pinapp.messaging.application.service.send.usecase;

import com.pinapp.messaging.domain.Notification;
import com.pinapp.messaging.domain.NotificationResult;

import java.util.concurrent.CompletableFuture;

public interface SendNotificationUseCase {

    <T extends Notification> NotificationResult send(T notification);

    <T extends Notification> CompletableFuture<NotificationResult> sendAsync(T notification);
}
