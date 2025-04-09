FROM gradle:8.13.0-jdk21 AS builder
WORKDIR /app

COPY settings.gradle settings.gradle
COPY build.gradle build.gradle
COPY paymentservice ./paymentservice
RUN gradle paymentservice:build -x test

FROM openjdk:21-jdk
WORKDIR /app
COPY --from=builder /app/paymentservice/build/libs/paymentservice-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]