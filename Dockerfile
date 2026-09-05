FROM gradle:8.7-jdk21-alpine AS builder
WORKDIR /build

COPY build.gradle.kts settings.gradle.kts gradle/ gradlew ./
COPY src ./src

RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]