FROM openjdk:8-jre-alpine
LABEL maintainer="Daniel Gresak (daniel.gresak@ucdconnect.ie)"
COPY target/kodakola-0.0.1-jar-with-dependencies.jar /kodakola-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/kodakola-0.0.1-jar-with-dependencies.jar", "lf.fleet.kodakola.Main"]