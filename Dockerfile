# --------------------
# Build Stage
# --------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 캐시 계층 최적화: 의존성 먼저 복사
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle ./gradle
COPY gradlew .

# 의존성만 먼저 다운 (실패해도 무시해서 캐시 유지)
RUN ./gradlew build -x test || return 0

# 실제 소스 복사 후 빌드
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