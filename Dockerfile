# =========================================================================
# GIAI ĐOẠN 1: Build file JAR từ mã nguồn Gradle gốc (Ép Mirror mạng + Cài Timezone)
# =========================================================================
FROM gradle:8.6-jdk17-alpine AS builder
WORKDIR /app

ENV GRADLE_OPTS="-Dhttp.socketTimeout=120000 -Dhttp.connectionTimeout=120000 -Dhttps.protocols=TLSv1.2,TLSv1.3 -Dorg.gradle.daemon=false"

# 🌟 BỔ SUNG QUAN TRỌNG: Cài đặt múi giờ Việt Nam cho cả Giai đoạn biên dịch code
RUN sed -i 's/dl-cdn.alpinelinux.org/mirror.fcix.net/g' /etc/apk/repositories \
    && apk update && apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime \
    && echo "Asia/Ho_Chi_Minh" > /etc/timezone

COPY . .
RUN gradle bootJar -x test --no-daemon --refresh-dependencies

# =========================================================================
# GIAI ĐOẠN 2: Tạo môi trường chạy ứng dụng tích hợp FFmpeg siêu nhẹ
# =========================================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Ép hệ thống dùng kho Mirror CDN và đồng bộ múi giờ hệ thống khi chạy runtime
RUN sed -i 's/dl-cdn.alpinelinux.org/mirror.fcix.net/g' /etc/apk/repositories \
    && apk update \
    && apk add --no-cache ffmpeg tzdata \
    && cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime \
    && echo "Asia/Ho_Chi_Minh" > /etc/timezone

COPY --from=builder /app/build/libs/*.jar app.jar

RUN mkdir -p /app/upload

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
