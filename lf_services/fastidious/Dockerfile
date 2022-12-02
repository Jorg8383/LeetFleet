FROM openjdk:8-jre-alpine
LABEL maintainer="Daniel Gresak (daniel.gresak@ucdconnect.ie)"
COPY target/fastidious-0.0.1-jar-with-dependencies.jar /fastidious-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/fastidious-0.0.1-jar-with-dependencies.jar", "lf.fleetman.FastidiousStart"]