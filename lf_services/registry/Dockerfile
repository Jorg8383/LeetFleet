FROM openjdk:8-jre-alpine
LABEL maintainer="Daniel Gresak (daniel.gresak@ucdconnect.ie)"
COPY target/registry-0.0.1-jar-with-dependencies.jar /registry-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/registry-0.0.1-jar-with-dependencies.jar", "lf.Main"]