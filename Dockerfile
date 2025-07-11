FROM eclipse-temurin:17-jre-alpine

RUN apk --no-cache add wget ca-certificates

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8080

EXPOSE 10090

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD ["wget", "--quiet", "--spider", "http://localhost:10090/actuator/health"]

ENTRYPOINT ["java", "-jar", "app.jar"]
