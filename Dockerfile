# --------------------
# Build Stage
# --------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 존재하는 파일만 복사
COPY build.gradle .
COPY settings.gradle .
COPY gradle ./gradle
COPY gradlew .

# 의존성 캐시를 위한 프리 빌드 (실패 무시)
RUN ./gradlew build -x test || return 0

# 전체 소스 복사 후 빌드
COPY . .

RUN ./gradlew build -x test

# --------------------
# Runtime Stage
# --------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]