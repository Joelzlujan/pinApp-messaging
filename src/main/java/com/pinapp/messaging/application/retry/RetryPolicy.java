package com.pinapp.messaging.application.retry;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RetryPolicy {

    @Builder.Default
    private final int maxAttempts = 1;

    @Builder.Default
    private final long delayMillis = 1000;

    @Builder.Default
    private final double backoffMultiplier = 1.0;

    public static RetryPolicy none() {
        return RetryPolicy.builder().maxAttempts(1).build();
    }

    public static RetryPolicy of(int maxAttempts, long delayMillis) {
        return RetryPolicy.builder()
                .maxAttempts(maxAttempts)
                .delayMillis(delayMillis)
                .build();
    }

    public static RetryPolicy withBackoff(int maxAttempts, long delayMillis, double multiplier) {
        return RetryPolicy.builder()
                .maxAttempts(maxAttempts)
                .delayMillis(delayMillis)
                .backoffMultiplier(multiplier)
                .build();
    }

    public long getDelayForAttempt(int attempt) {
        if (attempt <= 1) {
            return delayMillis;
        }
        return (long) (delayMillis * Math.pow(backoffMultiplier, attempt - 1));
    }
}
