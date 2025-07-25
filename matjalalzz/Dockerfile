FROM gradle:8.7.0-jdk21 AS build
WORKDIR /app
# 저장소 루트가 아니라 matjalalzz 서브폴더만 복사
COPY --chown=gradle:gradle . .
RUN gradle clean build -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV TZ=Asia/Seoul

EXPOSE 8700

COPY --from=build /app/build/libs/*.jar 500m_btn_be_1-0.0-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "500m_btn_be_1-0.0-SNAPSHOT.jar"]
