FROM openjdk:8-jre-alpine
LABEL maintainer="Tomás Kelly (tomas.kelly1@ucdconnect.ie)"
COPY target/client-0.0.1-jar-with-dependencies.jar /client-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/client-0.0.1-jar-with-dependencies.jar", "client.Main","-b","broker"]