# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Featherweight Alpine JRE Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8888

# Low-footprint JVM tuning for Render:
# - SerialGC saves ~250MB RAM compared to G1GC on low-resource tiers
# - Xss512k cuts per-thread stack memory
# - urandom ensures instant crypto init on container boot
ENTRYPOINT ["java", \
    "-XX:+UseSerialGC", \
    "-Xss512k", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
