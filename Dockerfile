# ---------- BUILD STAGE ----------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copy POM and download dependencies (cacheable)
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copy source
COPY src ./src

# Build JAR
RUN mvn -q -DskipTests package

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# Environment variables
ENV PORT=8080
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"

# Copy final jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]