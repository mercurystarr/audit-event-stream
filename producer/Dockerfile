FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY settings.gradle.kts build.gradle.kts gradlew ./
RUN chmod +x gradlew
COPY gradle gradle
COPY proto proto
COPY producer producer
COPY consumer consumer

RUN ./gradlew :producer:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /app/producer/build/libs/producer-0.1.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
