plugins {
    java
    id("org.springframework.boot") version "4.1.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "com.reynan"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        // --- RabbitMQ ---
        add("implementation", "org.springframework.boot:spring-boot-starter-amqp")
        add("testImplementation", "org.springframework.boot:spring-boot-starter-amqp-test")

        // --- Spring JPA ---
        add("implementation", "org.springframework.boot:spring-boot-starter-data-jpa")
        add("testImplementation", "org.springframework.boot:spring-boot-starter-data-jpa-test")

        // --- Flyway ---
        add("implementation", "org.springframework.boot:spring-boot-starter-flyway")
        add("implementation", "org.flywaydb:flyway-database-postgresql")
        add("testImplementation", "org.springframework.boot:spring-boot-starter-flyway-test")

        // --- Database - PostgreSQL ---
        add("runtimeOnly", "org.postgresql:postgresql")

        // --- DevTools & Docker Compose ---
        add("developmentOnly", "org.springframework.boot:spring-boot-devtools")
        add("developmentOnly", "org.springframework.boot:spring-boot-docker-compose")

        // --- Testes ---
        add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}