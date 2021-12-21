FROM openjdk:11
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} myJar.jar
ENTRYPOINT ["java","-jar","myJar.jar"]
