package com.pinapp.messaging.examples;

import com.pinapp.messaging.MessagingClient;
import com.pinapp.messaging.application.exception.ConfigurationException;
import com.pinapp.messaging.application.exception.ValidationException;
import com.pinapp.messaging.application.retry.RetryPolicy;
import com.pinapp.messaging.domain.NotificationResult;
import com.pinapp.messaging.domain.Recipient;
import com.pinapp.messaging.domain.email.EmailNotification;
import com.pinapp.messaging.domain.push.PushNotification;
import com.pinapp.messaging.domain.sms.SmsNotification;
import com.pinapp.messaging.infrastructure.config.ProviderCredentials;
import com.pinapp.messaging.infrastructure.event.LoggingEventPublisher;
import com.pinapp.messaging.infrastructure.provider.email.SendGridEmailSender;
import com.pinapp.messaging.infrastructure.provider.push.FirebasePushSender;
import com.pinapp.messaging.infrastructure.provider.sms.TwilioSmsSender;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Ejemplos de uso de PinApp Messaging Library.
 * Ejecutar: java -cp target/messaging-1.0.0.jar com.pinapp.messaging.examples.NotificationExamples
 */
public class NotificationExamples {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("PinApp Messaging Library - Ejemplos de Uso");
        System.out.println("=".repeat(60));
        System.out.println();

        // Ejemplo 1: Email básico
        emailBasico();

        // Ejemplo 2: SMS
        smsBasico();

        // Ejemplo 3: Push Notification
        pushBasico();

        // Ejemplo 4: Cliente con múltiples canales
        multiCanal();

        // Ejemplo 5: Async con Virtual Threads
        asyncExample();

        // Ejemplo 6: Retry con backoff
        retryExample();

        // Ejemplo 7: Manejo de errores
        errorHandling();

        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("Todos los ejemplos ejecutados correctamente");
        System.out.println("=".repeat(60));
    }

    private static void emailBasico() {
        System.out.println("\n--- Ejemplo 1: Email Básico ---\n");

        // Configurar credenciales
        ProviderCredentials credentials = ProviderCredentials.builder()
                .apiKey("SG.demo-api-key")
                .build();

        // Crear cliente con SendGrid
        MessagingClient client = MessagingClient.builder()
                .withEmailSender(new SendGridEmailSender(credentials))
                .build();

        // Crear notificación
        EmailNotification email = EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder()
                        .email("usuario@ejemplo.com")
                        .name("Usuario Demo")
                        .build())
                .subject("Bienvenido a PinApp")
                .body("Hola, gracias por registrarte en nuestra plataforma.")
                .fromEmail("noreply@pinapp.com")
                .fromName("PinApp")
                .build();

        // Enviar
        NotificationResult result = client.send(email);

        System.out.println("Email enviado: " + result.isSuccess());
        System.out.println("Provider ID: " + result.getProviderMessageId());
    }

    private static void smsBasico() {
        System.out.println("\n--- Ejemplo 2: SMS Básico ---\n");

        ProviderCredentials credentials = ProviderCredentials.builder()
                .accountSid("AC-demo-account-sid")
                .authToken("demo-auth-token")
                .fromNumber("+15551234567")
                .build();

        MessagingClient client = MessagingClient.builder()
                .withSmsSender(new TwilioSmsSender(credentials))
                .build();

        SmsNotification sms = SmsNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder()
                        .phoneNumber("+5491155551234")
                        .build())
                .body("Tu código de verificación es: 123456")
                .build();

        NotificationResult result = client.send(sms);

        System.out.println("SMS enviado: " + result.isSuccess());
        System.out.println("Provider ID: " + result.getProviderMessageId());
    }

    private static void pushBasico() {
        System.out.println("\n--- Ejemplo 3: Push Notification ---\n");

        ProviderCredentials credentials = ProviderCredentials.builder()
                .projectId("demo-firebase-project")
                .build();

        MessagingClient client = MessagingClient.builder()
                .withPushSender(new FirebasePushSender(credentials))
                .build();

        PushNotification push = PushNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder()
                        .deviceToken("demo-device-token-fcm")
                        .build())
                .title("Nueva promoción")
                .body("Tenés 20% de descuento en tu próxima compra")
                .data(Map.of("promo_id", "PROMO20", "expires", "2024-12-31"))
                .build();

        NotificationResult result = client.send(push);

        System.out.println("Push enviado: " + result.isSuccess());
        System.out.println("Provider ID: " + result.getProviderMessageId());
    }

    private static void multiCanal() {
        System.out.println("\n--- Ejemplo 4: Cliente Multi-Canal ---\n");

        // Configurar todos los proveedores
        ProviderCredentials emailCreds = ProviderCredentials.builder()
                .apiKey("SG.demo-key")
                .build();

        ProviderCredentials smsCreds = ProviderCredentials.builder()
                .accountSid("AC-demo")
                .authToken("token-demo")
                .fromNumber("+15551234567")
                .build();

        ProviderCredentials pushCreds = ProviderCredentials.builder()
                .projectId("demo-project")
                .build();

        // Cliente con los 3 canales
        MessagingClient client = MessagingClient.builder()
                .withEmailSender(new SendGridEmailSender(emailCreds))
                .withSmsSender(new TwilioSmsSender(smsCreds))
                .withPushSender(new FirebasePushSender(pushCreds))
                .withEventPublisher(new LoggingEventPublisher())
                .build();

        // Enviar por cada canal
        EmailNotification email = EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().email("test@test.com").build())
                .subject("Test multi-canal")
                .body("Email desde cliente multi-canal")
                .build();

        SmsNotification sms = SmsNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().phoneNumber("+5491155551234").build())
                .body("SMS desde cliente multi-canal")
                .build();

        PushNotification push = PushNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().deviceToken("device-token-12345").build())
                .title("Push multi-canal")
                .body("Push desde cliente multi-canal")
                .build();

        System.out.println("Enviando Email: " + client.send(email).isSuccess());
        System.out.println("Enviando SMS: " + client.send(sms).isSuccess());
        System.out.println("Enviando Push: " + client.send(push).isSuccess());
    }

    private static void asyncExample() {
        System.out.println("\n--- Ejemplo 5: Envío Asíncrono (Virtual Threads) ---\n");

        ProviderCredentials credentials = ProviderCredentials.builder()
                .apiKey("SG.demo")
                .build();

        MessagingClient client = MessagingClient.builder()
                .withEmailSender(new SendGridEmailSender(credentials))
                .build();

        // Crear 5 emails
        List<EmailNotification> emails = List.of(
                createEmail("usuario1@test.com", "Async 1"),
                createEmail("usuario2@test.com", "Async 2"),
                createEmail("usuario3@test.com", "Async 3"),
                createEmail("usuario4@test.com", "Async 4"),
                createEmail("usuario5@test.com", "Async 5")
        );

        System.out.println("Enviando 5 emails en paralelo...");
        long start = System.currentTimeMillis();

        // Enviar todos en paralelo
        List<CompletableFuture<NotificationResult>> futures = emails.stream()
                .map(client::sendAsync)
                .toList();

        // Esperar que todos terminen
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("5 emails enviados en " + elapsed + "ms (paralelo con Virtual Threads)");

        // Verificar resultados
        long exitosos = futures.stream()
                .map(CompletableFuture::join)
                .filter(NotificationResult::isSuccess)
                .count();
        System.out.println("Exitosos: " + exitosos + "/5");
    }

    private static void retryExample() {
        System.out.println("\n--- Ejemplo 6: Retry con Backoff Exponencial ---\n");

        ProviderCredentials credentials = ProviderCredentials.builder()
                .apiKey("SG.demo")
                .build();

        // Configurar retry: 3 intentos, 100ms inicial, backoff x2
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxAttempts(3)
                .delayMillis(100)
                .backoffMultiplier(2.0)
                .build();

        MessagingClient client = MessagingClient.builder()
                .withEmailSender(new SendGridEmailSender(credentials))
                .withRetryPolicy(retryPolicy)
                .withEventPublisher(new LoggingEventPublisher())
                .build();

        EmailNotification email = EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().email("retry@test.com").build())
                .subject("Test con Retry")
                .body("Este email tiene política de reintentos configurada")
                .build();

        System.out.println("Política: " + retryPolicy.getMaxAttempts() + " intentos, "
                + retryPolicy.getDelayMillis() + "ms delay, x"
                + retryPolicy.getBackoffMultiplier() + " backoff");

        NotificationResult result = client.send(email);
        System.out.println("Resultado: " + result.isSuccess());
    }

    private static void errorHandling() {
        System.out.println("\n--- Ejemplo 7: Manejo de Errores ---\n");

        ProviderCredentials credentials = ProviderCredentials.builder()
                .apiKey("SG.demo")
                .build();

        // Cliente solo con email (sin SMS ni Push)
        MessagingClient client = MessagingClient.builder()
                .withEmailSender(new SendGridEmailSender(credentials))
                .withValidation(true)
                .build();

        // Error 1: Email inválido (ValidationException)
        System.out.println("Test 1: Email con formato inválido");
        try {
            EmailNotification invalidEmail = EmailNotification.builder()
                    .id(UUID.randomUUID().toString())
                    .recipient(Recipient.builder().email("no-es-email").build())
                    .subject("Test")
                    .body("Body")
                    .build();
            client.send(invalidEmail);
        } catch (ValidationException e) {
            System.out.println("  -> ValidationException: " + e.getMessage());
        }

        // Error 2: Canal no configurado (ConfigurationException)
        System.out.println("\nTest 2: Enviar SMS sin sender configurado");
        try {
            SmsNotification sms = SmsNotification.builder()
                    .id(UUID.randomUUID().toString())
                    .recipient(Recipient.builder().phoneNumber("+5491155551234").build())
                    .body("Test")
                    .build();
            client.send(sms);
        } catch (ConfigurationException e) {
            System.out.println("  -> ConfigurationException: " + e.getMessage());
        }

        System.out.println("\nErrores manejados correctamente");
    }

    private static EmailNotification createEmail(String to, String subject) {
        return EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .recipient(Recipient.builder().email(to).build())
                .subject(subject)
                .body("Contenido del email " + subject)
                .build();
    }
}
