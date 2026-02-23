# =====================================
# Stage 1: Build
# =====================================
FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /app

# Copy Maven wrapper và pom.xml để cache dependencies
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies (sẽ được cache nếu pom.xml không đổi)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application (skip tests để build nhanh hơn)
RUN mvn clean package -DskipTests -B

# =====================================
# Stage 2: Runtime
# =====================================
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Tạo user non-root để chạy app (security best practice)
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy jar file từ build stage
COPY --from=build /app/target/*.jar app.jar

# Đổi ownership cho user non-root
RUN chown -R appuser:appgroup /app

# Chuyển sang user non-root
USER appuser

# Expose ports
# 8082: HTTP REST API
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# JVM options để optimize container
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
