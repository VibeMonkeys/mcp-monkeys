FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

COPY mcp-employee-server/build.gradle.kts mcp-employee-server/
COPY shared/build.gradle.kts shared/

RUN chmod +x ./gradlew
RUN ./gradlew :mcp-employee-server:dependencies --no-daemon || true

COPY shared/src shared/src
COPY mcp-employee-server/src mcp-employee-server/src

RUN ./gradlew :mcp-employee-server:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /app/mcp-employee-server/build/libs/*.jar app.jar

ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8097

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
