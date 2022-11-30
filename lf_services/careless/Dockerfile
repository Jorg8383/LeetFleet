FROM openjdk:8-jre-alpine
LABEL maintainer="Daniel Gresak (daniel.gresak@ucdconnect.ie)"
COPY target/careless-0.0.1-jar-with-dependencies.jar /careless-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/careless-0.0.1-jar-with-dependencies.jar", "lf.fleetman.CarelessStart"]