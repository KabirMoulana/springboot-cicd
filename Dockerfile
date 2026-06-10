# ─────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and pom first for layer caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies in a separate layer (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B -q

# Copy source and build
COPY src src
RUN ./mvnw package -B -DskipTests -q && \
    java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# ─────────────────────────────────────────────
# Stage 2: Runtime
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy layered JAR components for optimal Docker caching
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/dependencies ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/spring-boot-loader ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/snapshot-dependencies ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/application ./

USER appuser

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Use exec form to ensure signal handling
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-default}", \
  "org.springframework.boot.loader.launch.JarLauncher"]
