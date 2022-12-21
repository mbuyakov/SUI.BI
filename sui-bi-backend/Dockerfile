FROM openjdk:17-jdk-alpine
ARG JAR_FILE=./backend/build/libs/*.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=prod"]
EXPOSE 8080