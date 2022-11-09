FROM openjdk:8-jre-alpine
LABEL maintainer="Tomás Kelly (tomas.kelly1@ucdconnect.ie)"
COPY target/auldfellas-0.0.1-jar-with-dependencies.jar /auldfellas-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/auldfellas-0.0.1-jar-with-dependencies.jar", "service.auldfellas.Main","-b","broker"]