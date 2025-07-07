FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8080

EXPOSE 10090

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD ["curl", "-f", "http://localhost:10090/actuator/health"]

ENTRYPOINT ["java", "-jar", "app.jar"]
