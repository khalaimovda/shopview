FROM gradle:8.13.0-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test

FROM openjdk:21-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/shopview-0.0.1-SNAPSHOT.jar app.jar

# Copy images for test data. Comment it it is not necessary
#############################################################
RUN mkdir -p /tmp/uploads/images
COPY ./infra/x_image.png /tmp/uploads/images/x_image.png
RUN for i in {1..25}; do \
        cp /tmp/uploads/images/x_image.png /tmp/uploads/images/image_path_$i.png; \
    done
#############################################################

CMD ["java", "-jar", "app.jar"]