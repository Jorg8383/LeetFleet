FROM openjdk:8-jre-alpine
LABEL maintainer="Daniel Gresak (daniel.gresak@ucdconnect.ie)"
COPY target/fleetless-0.0.1-jar-with-dependencies.jar /fleetless-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/fleetless-0.0.1-jar-with-dependencies.jar", "lf.fleetman.FleetlessStart"]