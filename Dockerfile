FROM eclipse-temurin:17-jre-alpine AS base

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8080

EXPOSE 10090

ENTRYPOINT ["java", "-jar", "app.jar"]


FROM base AS dev

HEALTHCHECK --start-period=90s --interval=15s --timeout=20s --retries=3 \
  CMD ["wget", "--quiet", "--spider", "http://localhost:8080/actuator/health"]


FROM base AS prod

HEALTHCHECK --start-period=90s --interval=15s --timeout=5s --retries=3 \
  CMD ["wget", "--quiet", "--spider", "http://localhost:10090/actuator/health"]
