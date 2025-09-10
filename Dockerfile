# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# (Better layer caching) copy pom first, then src
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# Find the built jar (adjust if your jar name is fixed)
RUN ls -1 target/*.jar | tail -n1 > /tmp/jarfile

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre

# Create non-root user
RUN useradd -ms /bin/bash appuser
USER appuser
WORKDIR /app

# Copy jar from builder
COPY --from=build /tmp/jarfile /app/jarfile
RUN cp "$(cat /app/jarfile)" /app/app.jar && rm /app/jarfile

# Optional: default JVM opts; Render will set $PORT
# ENV JAVA_OPTS="-Xms256m -Xmx512m"
# ENV SPRING_PROFILES_ACTIVE=prod

# Healthcheck (requires actuator; change path or remove if unused)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
#   CMD wget -qO- "http://127.0.0.1:${PORT:-8080}/actuator/health" || exit 1

# Start app. Render injects $PORT; we forward it to Spring.
CMD sh -c 'java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar /app/app.jar'
