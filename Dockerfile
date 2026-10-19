FROM eclipse-temurin:25
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY /target/taskrail-0.0.1.jar taskrail.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "taskrail.jar"]