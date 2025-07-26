FROM eclipse-temurin:17-jre-alpine AS base

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar

COPY --chown=appuser:appgroup ${JAR_FILE} app.jar

USER appuser

EXPOSE 8080

EXPOSE 10090

ENTRYPOINT ["java", "-jar", "app.jar"]


FROM base AS dev

HEALTHCHECK --start-period=90s --interval=20s --timeout=20s --retries=3 \
  CMD ["wget", "--quiet", "--spider", "http://localhost:8080/server/actuator/health"]


FROM base AS stg

HEALTHCHECK --start-period=90s --interval=15s --timeout=5s --retries=3 \
  CMD ["wget", "--quiet", "--spider", "http://localhost:10090/server/actuator/health"]


FROM base AS prod

HEALTHCHECK --start-period=90s --interval=15s --timeout=5s --retries=3 \
  CMD ["wget", "--quiet", "--spider", "http://localhost:10090/server/actuator/health"]
