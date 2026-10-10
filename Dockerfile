# Build the application
FROM maven:3.9.16-eclipse-temurin-25-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw clean package -DskipTests

# Create a custom Java runtime
FROM eclipse-temurin:25-jdk-alpine AS jre-build

RUN $JAVA_HOME/bin/jlink \
    --add-modules java.base,java.desktop,java.logging,java.naming,java.sql,java.xml,java.management,java.instrument,java.security.jgss,java.net.http,java.compiler,jdk.unsupported \
    --strip-debug \
    --compress=2 \
    --no-header-files \
    --no-man-pages \
    --output /javaruntime

# Create runtime image
FROM alpine:3.24.1

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="$JAVA_HOME/bin:$PATH"

COPY --from=jre-build /javaruntime $JAVA_HOME

RUN apk add --no-cache wget
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=build --chown=spring:spring /app/target/*.jar /app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]