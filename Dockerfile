FROM --platform=linux/amd64 openjdk:11
COPY build/libs/esc-0.0.1-SNAPSHOT.jar esc-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "esc-0.0.1-SNAPSHOT.jar"]