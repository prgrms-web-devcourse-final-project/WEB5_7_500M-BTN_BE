FROM gradle:8.7.0-jdk21 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle build -x test --no-build-cache

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar 500m_btn_be_1-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "500m_btn_be_1-0.0.1-SNAPSHOT.jar"]
