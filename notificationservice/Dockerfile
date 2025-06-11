FROM openjdk:21-jdk-slim

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]

# Раскомментируйте, если сервис слушает не 8080 по умолчанию
EXPOSE 8083