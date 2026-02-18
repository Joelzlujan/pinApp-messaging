package com.pinapp.messaging.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Recipient {

    private final String email;
    private final String phoneNumber;
    private final String deviceToken;
    private final String name;
}
