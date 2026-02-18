package com.pinapp.messaging.application.validation;

import com.pinapp.messaging.application.exception.ValidationException;
import com.pinapp.messaging.domain.Recipient;
import com.pinapp.messaging.domain.sms.SmsNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneValidatorTest {

    private PhoneValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PhoneValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"+5491155551234", "5491155551234", "+1234567890", "1234567890123"})
    void shouldPassValidPhoneNumbers(String phoneNumber) {
        SmsNotification sms = SmsNotification.builder()
                .id("1")
                .recipient(Recipient.builder().phoneNumber(phoneNumber).build())
                .body("Test message")
                .build();

        assertThatCode(() -> validator.validate(sms))
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
        SmsNotification sms = SmsNotification.builder()
                .id("1")
                .body("Test message")
                .build();

        assertThatThrownBy(() -> validator.validate(sms))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid phone number");
    }

    @Test
    void shouldThrowForNullPhoneNumber() {
        SmsNotification sms = SmsNotification.builder()
                .id("1")
                .recipient(Recipient.builder().build())
                .body("Test message")
                .build();

        assertThatThrownBy(() -> validator.validate(sms))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid phone number");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "abcdefghij", "+0123456789", ""})
    void shouldThrowForInvalidPhoneNumbers(String phoneNumber) {
        SmsNotification sms = SmsNotification.builder()
                .id("1")
                .recipient(Recipient.builder().phoneNumber(phoneNumber).build())
                .body("Test message")
                .build();

        assertThatThrownBy(() -> validator.validate(sms))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid phone number");
    }

    @Test
    void shouldThrowForMissingBody() {
        SmsNotification sms = SmsNotification.builder()
                .id("1")
                .recipient(Recipient.builder().phoneNumber("+5491155551234").build())
                .build();

        assertThatThrownBy(() -> validator.validate(sms))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("SMS body is required");
    }

    @Test
    void shouldThrowForBlankBody() {
        SmsNotification sms = SmsNotification.builder()
                .id("1")
                .recipient(Recipient.builder().phoneNumber("+5491155551234").build())
                .body("   ")
                .build();

        assertThatThrownBy(() -> validator.validate(sms))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("SMS body is required");
    }

    @Test
    void shouldReturnCorrectNotificationType() {
        assertThatCode(() -> {
            Class<SmsNotification> type = validator.getNotificationType();
            assert type.equals(SmsNotification.class);
        }).doesNotThrowAnyException();
    }
}
