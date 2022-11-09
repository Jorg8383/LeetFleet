FROM openjdk:8-jre-alpine
LABEL maintainer="Tomás Kelly (tomas.kelly1@ucdconnect.ie)"
COPY target/broker-0.0.1-jar-with-dependencies.jar /broker-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/broker-0.0.1-jar-with-dependencies.jar", "service.broker.Main","-t","5"]