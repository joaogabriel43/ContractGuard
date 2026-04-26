# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml first to leverage Docker layer cache for dependencies.
# The dependency download step is only re-run when pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build.
# -Dmaven.test.skip=true skips both test compilation and execution,
# which is safer than -DskipTests (which still compiles tests).
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true -B

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
