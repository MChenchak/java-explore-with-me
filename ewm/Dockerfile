ARG BASE_IMAGE="openjdk:11"

FROM $BASE_IMAGE

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]