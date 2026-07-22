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

# JVM tuned cho EC2 t3.micro (1GB RAM):
# - Xms128m: khởi động nhẹ
# - Xmx384m: giới hạn heap tối đa 384MB
# - G1GC: garbage collector tối ưu cho memory nhỏ
# - Profile mặc định: aws (override qua env SPRING_PROFILES_ACTIVE)
ENTRYPOINT ["java", "-jar", \
  "-Xms128m", "-Xmx384m", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=100", \
  "-XX:+UseStringDeduplication", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:aws}", \
  "app.jar"]
