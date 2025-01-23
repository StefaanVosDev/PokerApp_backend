FROM openjdk:17-jdk-slim
WORKDIR /
ARG JAR_FILE=/build/libs/poker-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]