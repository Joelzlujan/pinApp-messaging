package com.pinapp.messaging.application.validation;

import com.pinapp.messaging.application.exception.ValidationException;
import com.pinapp.messaging.domain.Recipient;
import com.pinapp.messaging.domain.email.EmailNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailValidatorTest {

    private EmailValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EmailValidator();
    }

    @Test
    void shouldPassValidEmail() {
        EmailNotification email = EmailNotification.builder()
                .id("1")
                .recipient(Recipient.builder().email("valid@example.com").build())
                .subject("Test")
                .body("Body")
                .build();

        assertThatCode(() -> validator.validate(email))
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
        EmailNotification email = EmailNotification.builder()
                .id("1")
                .subject("Test")
                .body("Body")
                .build();

        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email address");
    }

    @Test
    void shouldThrowForNullEmail() {
        EmailNotification email = EmailNotification.builder()
                .id("1")
                .recipient(Recipient.builder().build())
                .subject("Test")
                .body("Body")
                .build();

        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email address");
    }

    @Test
    void shouldThrowForInvalidEmail() {
        EmailNotification email = EmailNotification.builder()
                .id("1")
                .recipient(Recipient.builder().email("invalid-email").build())
                .subject("Test")
                .body("Body")
                .build();

        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email address");
    }

    @Test
    void shouldThrowForMissingSubject() {
        EmailNotification email = EmailNotification.builder()
                .id("1")
                .recipient(Recipient.builder().email("valid@example.com").build())
                .body("Body")
                .build();

        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("subject is required");
    }

    @Test
    void shouldThrowForBlankSubject() {
        EmailNotification email = EmailNotification.builder()
                .id("1")
                .recipient(Recipient.builder().email("valid@example.com").build())
                .subject("   ")
                .body("Body")
                .build();

        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("subject is required");
    }

    @Test
    void shouldThrowForMissingBody() {
        EmailNotification email = EmailNotification.builder()
                .id("1")
                .recipient(Recipient.builder().email("valid@example.com").build())
                .subject("Subject")
                .build();

        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("body is required");
    }

    @Test
    void shouldThrowForBlankBody() {
        EmailNotification email = EmailNotification.builder()
                .id("1")
                .recipient(Recipient.builder().email("valid@example.com").build())
                .subject("Subject")
                .body("")
                .build();

        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("body is required");
    }

    @Test
    void shouldReturnCorrectNotificationType() {
        assertThatCode(() -> {
            Class<EmailNotification> type = validator.getNotificationType();
            assert type.equals(EmailNotification.class);
        }).doesNotThrowAnyException();
    }
}
