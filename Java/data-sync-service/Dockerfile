# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

# Cache Maven dependencies before copying source (layer caching optimisation)
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B --no-transfer-progress 2>/dev/null || true

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -B --no-transfer-progress

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S syncgroup && adduser -S syncuser -G syncgroup
USER syncuser

# Copy the fat JAR from the build stage
COPY --from=build /workspace/target/*.jar app.jar

# Management port (actuator) is 8088; main API port is 8087
EXPOSE 8087 8088

# JVM tuning for containerised environments
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
