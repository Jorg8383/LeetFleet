FROM openjdk:8-jre-alpine
LABEL maintainer="Tomás Kelly (tomas.kelly1@ucdconnect.ie)"
COPY target/dodgydrivers-0.0.1-jar-with-dependencies.jar /dodgydrivers-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/dodgydrivers-0.0.1-jar-with-dependencies.jar","service.dodgydrivers.Main","-b","broker"]