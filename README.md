# PinApp Messaging Library

Librería de notificaciones en Java, agnóstica a frameworks. Soporta Email, SMS y Push con múltiples proveedores.

## Requisitos

- Java 21+
- Maven 3.8+

## Instalación

```bash
mvn clean install
```

O con Docker (sin necesidad de Java local):

```bash
docker build -t pinapp-messaging .
docker run pinapp-messaging
```

---

## Uso Rápido

```java
// Configurar cliente con SendGrid
MessagingClient client = MessagingClient.builder()
    .withEmailSender(new SendGridEmailSender(
        ProviderCredentials.builder()
            .apiKey("SG.tu-api-key")
            .build()
    ))
    .build();

// Crear y enviar email
EmailNotification email = EmailNotification.builder()
    .id(UUID.randomUUID().toString())
    .recipient(Recipient.builder().email("usuario@ejemplo.com").build())
    .subject("Bienvenido")
    .body("Gracias por registrarte.")
    .build();

NotificationResult result = client.send(email);

if (result.isSuccess()) {
    System.out.println("Enviado: " + result.getProviderMessageId());
}
```

---

## Proveedores Soportados

| Canal | Proveedores |
|-------|-------------|
| Email | SendGrid, Mailgun |
| SMS | Twilio, Nexmo |
| Push | Firebase |

### Configuración por proveedor

**SendGrid:**
```java
new SendGridEmailSender(ProviderCredentials.builder()
    .apiKey("SG.xxx")
    .build())
```

**Mailgun:**
```java
new MailgunEmailSender(ProviderCredentials.builder()
    .apiKey("key-xxx")
    .domain("mg.tudominio.com")
    .build())
```

**Twilio:**
```java
new TwilioSmsSender(ProviderCredentials.builder()
    .accountSid("ACxxx")
    .authToken("tu-auth-token")
    .fromNumber("+1234567890")
    .build())
```

**Nexmo:**
```java
new NexmoSmsSender(ProviderCredentials.builder()
    .apiKey("xxx")
    .apiSecret("yyy")
    .fromNumber("+1234567890")
    .build())
```

**Firebase:**
```java
new FirebasePushSender(ProviderCredentials.builder()
    .projectId("mi-proyecto")
    .build())
```

---

## Cliente Multi-Canal

```java
MessagingClient client = MessagingClient.builder()
    .withEmailSender(new SendGridEmailSender(emailCreds))
    .withSmsSender(new TwilioSmsSender(smsCreds))
    .withPushSender(new FirebasePushSender(pushCreds))
    .build();

// El cliente detecta el tipo y usa el sender correspondiente
client.send(emailNotification);
client.send(smsNotification);
client.send(pushNotification);
```

---

## Validación

Habilitada por defecto. Valida formato de email, teléfono E.164, y tokens de push.

```java
// Deshabilitar si querés manejar validación externamente
MessagingClient.builder()
    .withEmailSender(sender)
    .withValidation(false)
    .build();
```

---

## Envío Asíncrono

Usa Virtual Threads de Java 21 internamente.

```java
// Envío no bloqueante
CompletableFuture<NotificationResult> future = client.sendAsync(email);

// Esperar resultado
NotificationResult result = future.get();

// O con callback
client.sendAsync(email)
    .thenAccept(r -> System.out.println("Enviado: " + r.getProviderMessageId()));
```

**Envío masivo en paralelo:**
```java
List<CompletableFuture<NotificationResult>> futures = emails.stream()
    .map(client::sendAsync)
    .toList();

CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
```

---

## Reintentos

```java
// 3 intentos con backoff exponencial (1s → 2s → 4s)
RetryPolicy policy = RetryPolicy.builder()
    .maxAttempts(3)
    .delayMillis(1000)
    .backoffMultiplier(2.0)
    .build();

MessagingClient client = MessagingClient.builder()
    .withEmailSender(sender)
    .withRetryPolicy(policy)
    .build();
```

---

## Eventos

Para logging, métricas o integración con otros sistemas:

```java
MessagingClient client = MessagingClient.builder()
    .withEmailSender(sender)
    .withEventPublisher(new LoggingEventPublisher()) // Incluido
    .build();
```

Eventos: `SENDING`, `SUCCESS`, `FAILED`, `RETRYING`

**Implementación custom:**
```java
public class MiPublisher implements EventPublisher {
    @Override
    public void publish(NotificationEvent event) {
        // Enviar a métricas, alertas, etc.
    }
}
```

---

## Manejo de Errores

```java
try {
    NotificationResult result = client.send(notification);
    if (!result.isSuccess()) {
        log.error("Fallo: {}", result.getErrorMessage());
    }
} catch (ValidationException e) {
    // Email mal formado, teléfono inválido, etc.
} catch (ConfigurationException e) {
    // No hay sender configurado para ese tipo
} catch (ProviderException e) {
    // Error de red o del proveedor
}
```

---

## Agregar un Proveedor Nuevo

Implementar `NotificationSender<T>`:

```java
public class MiEmailSender implements NotificationSender<EmailNotification> {

    @Override
    public NotificationResult send(EmailNotification notification) {
        // Llamada HTTP al proveedor
        return NotificationResult.success("provider-id-123");
    }

    @Override
    public Class<EmailNotification> getNotificationType() {
        return EmailNotification.class;
    }

    @Override
    public String getProviderName() {
        return "MiProveedor";
    }
}
```

Y registrarlo:
```java
client = MessagingClient.builder()
    .withEmailSender(new MiEmailSender(credentials))
    .build();
```

---

## Integración con Google Cloud Pub/Sub (Opcional)

Las dependencias están marcadas como `optional` - solo se incluyen si las agregás explícitamente.

**Publicar eventos:**
```java
PubSubConfig config = PubSubConfig.builder()
    .projectId("mi-proyecto-gcp")
    .statusTopic("notification-status")
    .build();

MessagingClient client = MessagingClient.builder()
    .withEmailSender(sender)
    .withEventPublisher(new PubSubEventPublisher(config))
    .build();
```

**Recibir requests:**
```java
PubSubMessageSubscriber subscriber = new PubSubMessageSubscriber(config, client);
subscriber.start();
```

---

## Tests

```bash
mvn test
```

63 tests unitarios cubriendo validadores, proveedores y el cliente.

---

## Docker

El Dockerfile permite ejecutar los ejemplos sin Java instalado:

```bash
docker build -t pinapp-messaging .
docker run pinapp-messaging
```

Muestra 7 ejemplos: Email, SMS, Push, Multi-Canal, Async, Retry y Error Handling.

---

## Arquitectura

```
src/main/java/com/pinapp/messaging/
├── domain/                 # Entidades (Notification, Recipient, Result)
├── application/            # Lógica de negocio
│   ├── exception/          # ValidationException, ProviderException...
│   ├── port/               # Interfaces (NotificationSender, EventPublisher)
│   ├── validation/         # Validadores por canal
│   └── service/            # SendNotificationService
├── infrastructure/         # Implementaciones
│   ├── provider/           # SendGrid, Twilio, Firebase...
│   └── pubsub/             # Integración Pub/Sub (opcional)
└── MessagingClient.java    # Entry point
```

### Decisiones de diseño

**Clean Architecture**: El dominio no depende de proveedores externos. Si SendGrid cambia su API, solo tocamos `SendGridEmailSender`.

**Campos tipados en lugar de Map genérico**: Usé campos específicos (`emailSender`, `smsSender`, `pushSender`) en lugar de un `Map<Class<?>, NotificationSender<?>>`. Es más explícito, type-safe, y no requiere `@SuppressWarnings`.

**Builder pattern**: Configuración 100% en código, sin archivos YAML. El usuario controla exactamente qué proveedores usa.

**Providers simulados**: Los senders retornan resultados simulados. En producción harían llamadas HTTP reales. Esto permite correr tests sin credenciales y enfocarse en el diseño.

**Virtual Threads**: Java 21 los hace livianos. `sendAsync()` puede manejar miles de envíos concurrentes sin pool tuning.

**Dependencias opcionales**: Pub/Sub está marcado como `<optional>true</optional>`. Si no lo usás, no te pesa.

- Sin optional: Le descarga automáticamente Pub/Sub (~50 dependencias de Google)

- Con optional: NO le descarga Pub/Sub a la persona que integre nuestra libreria. Solo si él lo necesita, lo agrega manualmente.

Es para no forzar dependencias pesadas a quien solo quiere usar SendGrid.


---

## Principios SOLID

| Principio | Aplicación |
|-----------|------------|
| Single Responsibility | `EmailValidator` valida, `SendGridEmailSender` envía |
| Open/Closed | Agregar proveedor = nueva clase, sin tocar código existente |
| Liskov Substitution | SendGrid y Mailgun son intercambiables |
| Interface Segregation | `NotificationSender<T>` tiene solo lo necesario |
| Dependency Inversion | El servicio depende de interfaces, no de implementaciones |
