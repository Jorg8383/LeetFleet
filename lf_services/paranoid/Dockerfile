FROM openjdk:8-jre-alpine
LABEL maintainer="Daniel Gresak (daniel.gresak@ucdconnect.ie)"
COPY target/paranoid-0.0.1-jar-with-dependencies.jar /paranoid-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/paranoid-0.0.1-jar-with-dependencies.jar", "lf.fleet.paranoid.Main"]