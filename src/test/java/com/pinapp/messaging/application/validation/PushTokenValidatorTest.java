package com.pinapp.messaging.application.validation;

import com.pinapp.messaging.application.exception.ValidationException;
import com.pinapp.messaging.domain.Recipient;
import com.pinapp.messaging.domain.push.PushNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PushTokenValidatorTest {

    private PushTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PushTokenValidator();
    }

    @Test
    void shouldPassValidPushNotification() {
        PushNotification push = PushNotification.builder()
                .id("1")
                .recipient(Recipient.builder().deviceToken("valid-device-token-12345").build())
                .title("Test Title")
                .body("Test Body")
                .build();

        assertThatCode(() -> validator.validate(push))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowForNullNotification() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Notification cannot be null");
    }

    @Test
    void shouldThrowForNullRecipient() {
        PushNotification push = PushNotification.builder()
                .id("1")
                .title("Title")
                .body("Body")
                .build();

        assertThatThrownBy(() -> validator.validate(push))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid device token");
    }

    @Test
    void shouldThrowForNullDeviceToken() {
        PushNotification push = PushNotification.builder()
                .id("1")
                .recipient(Recipient.builder().build())
                .title("Title")
                .body("Body")
                .build();

        assertThatThrownBy(() -> validator.validate(push))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid device token");
    }

    @Test
    void shouldThrowForShortDeviceToken() {
        PushNotification push = PushNotification.builder()
                .id("1")
                .recipient(Recipient.builder().deviceToken("short").build())
                .title("Title")
                .body("Body")
                .build();

        assertThatThrownBy(() -> validator.validate(push))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid device token");
    }

    @Test
    void shouldThrowForMissingTitle() {
        PushNotification push = PushNotification.builder()
                .id("1")
                .recipient(Recipient.builder().deviceToken("valid-device-token-12345").build())
                .body("Body")
                .build();

        assertThatThrownBy(() -> validator.validate(push))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("title is required");
    }

    @Test
    void shouldThrowForBlankTitle() {
        PushNotification push = PushNotification.builder()
                .id("1")
                .recipient(Recipient.builder().deviceToken("valid-device-token-12345").build())
                .title("   ")
                .body("Body")
                .build();

        assertThatThrownBy(() -> validator.validate(push))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("title is required");
    }

    @Test
    void shouldThrowForMissingBody() {
        PushNotification push = PushNotification.builder()
                .id("1")
                .recipient(Recipient.builder().deviceToken("valid-device-token-12345").build())
                .title("Title")
                .build();

        assertThatThrownBy(() -> validator.validate(push))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("body is required");
    }

    @Test
    void shouldReturnCorrectNotificationType() {
        assertThatCode(() -> {
            Class<PushNotification> type = validator.getNotificationType();
            assert type.equals(PushNotification.class);
        }).doesNotThrowAnyException();
    }
}
