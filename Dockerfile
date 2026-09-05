FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle/ ./gradle/
COPY order-service/build.gradle.kts order-service/build.gradle.kts
COPY payment-service/build.gradle.kts payment-service/build.gradle.kts
COPY inventory-service/build.gradle.kts inventory-service/build.gradle.kts
COPY notification-service/build.gradle.kts notification-service/build.gradle.kts

COPY order-service/src order-service/src
COPY payment-service/src payment-service/src
COPY inventory-service/src inventory-service/src
COPY notification-service/src notification-service/src

ARG SERVICE_NAME
RUN chmod +x gradlew && ./gradlew :${SERVICE_NAME}:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG SERVICE_NAME
COPY --from=builder /build/${SERVICE_NAME}/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]