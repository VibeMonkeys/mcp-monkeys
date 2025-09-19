FROM eclipse-temurin:21-jre-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 필요한 패키지 설치
RUN apk add --no-cache curl

# 애플리케이션 JAR 파일 복사
COPY mcp-slack-server/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8096

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8096/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", \
  "-jar", \
  "app.jar"]