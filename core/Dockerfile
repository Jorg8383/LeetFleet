FROM openjdk:8-jre-alpine
LABEL maintainer="Daniel Gresak (daniel.gresak@ucdconnect.ie)"
COPY target/core-0.0.1-jar-with-dependencies.jar /core-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/core-0.0.1-jar-with-dependencies.jar", "lf.core.HttpToAkka"]