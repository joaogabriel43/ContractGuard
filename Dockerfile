# syntax=docker/dockerfile:1

# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Cache Maven local repository with BuildKit cache mount.
# Unlike layer caching, this mount is never baked into the image and is
# shared across all builds — avoiding the stale-layer problem where
# dependency:go-offline was cached but annotation-processor JARs were missing.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

# Copy source and build.
# -Dmaven.test.skip=true skips both test compilation and execution,
# which is safer than -DskipTests (which still compiles tests).
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -Dmaven.test.skip=true -B

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
