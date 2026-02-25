FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src ./src
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar --no-daemon --stacktrace
EXPOSE 8080
CMD ["sh", "-c", "java -Dserver.address=0.0.0.0 -Dserver.port=${PORT} -jar build/libs/*SNAPSHOT.jar"]