package com.pinapp.messaging.application.exception;

public class ProviderException extends MessagingException {

    private final String providerName;

    public ProviderException(String providerName, String message) {
        super(message);
        this.providerName = providerName;
    }

    public ProviderException(String providerName, String message, Throwable cause) {
        super(message, cause);
        this.providerName = providerName;
    }

    public String getProviderName() {
        return providerName;
    }
}
