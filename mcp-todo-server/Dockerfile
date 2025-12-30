FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

COPY mcp-todo-server/build.gradle.kts mcp-todo-server/
COPY shared/build.gradle.kts shared/

RUN chmod +x ./gradlew
RUN ./gradlew :mcp-todo-server:dependencies --no-daemon || true

COPY shared/src shared/src
COPY mcp-todo-server/src mcp-todo-server/src

RUN ./gradlew :mcp-todo-server:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /app/mcp-todo-server/build/libs/*.jar app.jar

ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8096

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
