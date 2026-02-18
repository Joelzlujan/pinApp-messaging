FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Instalar Maven
RUN apk add --no-cache maven

# Copiar archivos del proyecto
COPY pom.xml .
COPY src ./src

# Compilar la librería
RUN mvn clean package -DskipTests

# Imagen final más liviana
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar JAR compilado
COPY --from=builder /app/target/messaging-1.0.0.jar app.jar

# Ejecutar ejemplos (fat JAR con todas las dependencias)
CMD ["java", "-jar", "app.jar"]
