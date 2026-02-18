package com.pinapp.messaging.domain;

import java.util.Map;

public interface Notification {

    String getId();

    Recipient getRecipient();

    String getBody();

    Map<String, Object> getMetadata();
}
