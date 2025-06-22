# Gradle + JDK가 함께 설치된 베이스 이미지
FROM gradle:8.5.0-jdk17-alpine AS builder

WORKDIR /app

# Gradle 캐시 최적화
COPY build.gradle settings.gradle gradle.properties* ./
COPY gradle ./gradle
RUN gradle --no-daemon clean

# 전체 소스 복사
COPY . .

# 🔥 테스트를 생략하고 빌드만 실행
RUN gradle build -x test --no-daemon

# --- 최종 실행 이미지 ---
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 업로드 파일 디렉토리 공유
VOLUME ["/app/uploads"]

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
