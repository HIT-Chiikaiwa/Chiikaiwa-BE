# =============================================================
# Chiikaiwa-BE — AWS Run-Only Dockerfile
# =============================================================
# Image này KHÔNG chứa Maven build.
# JAR được build bởi GitHub Actions, sau đó COPY vào đây.
# => Server AWS chỉ pull & run, không tốn CPU/RAM cho build.
# =============================================================

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Tạo user non-root để chạy app (Best practice bảo mật)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR đã được build bởi GitHub Actions
COPY target/Chiikaiwa-BE-0.0.1-SNAPSHOT.jar app.jar

# Port mặc định Spring Boot
EXPOSE 8080

# JVM tuned cho EC2 t3.small (2GB RAM):

ENTRYPOINT ["java", "-jar", \
  "-Xms384m", "-Xmx768m", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=100", \
  "-XX:+UseStringDeduplication", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:aws}", \
  "app.jar"]
