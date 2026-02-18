package com.pinapp.messaging.application.port;

public interface MessageSubscriber {

    void start();

    void stop();

    boolean isRunning();
}
