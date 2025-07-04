# Build Stage
FROM gradle:8.14.2-jdk24 AS build
LABEL MAINTAINER="pritamkundu771@gmail.com"
COPY service /home/gradle/service
WORKDIR /home/gradle/service
RUN gradle bootJar --no-daemon

# Runtime Stage
FROM amazoncorretto:24-alpine3.21-jdk
WORKDIR /app
COPY --from=build /home/gradle/service/build/libs/*.jar app.jar
EXPOSE 8080

# Health Check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT [ "java", "-jar", "app.jar" ]
