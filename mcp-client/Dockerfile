FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Gradle wrapper 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 모듈별 build.gradle.kts 복사
COPY mcp-client/build.gradle.kts mcp-client/
COPY shared/build.gradle.kts shared/

# 의존성 다운로드 (캐시 활용)
RUN chmod +x ./gradlew
RUN ./gradlew :mcp-client:dependencies --no-daemon || true

# 소스 코드 복사
COPY shared/src shared/src
COPY mcp-client/src mcp-client/src

# 빌드
RUN ./gradlew :mcp-client:bootJar --no-daemon -x test

# 런타임 이미지
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /app/mcp-client/build/libs/*.jar app.jar

# 환경 변수
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8090

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
