FROM openjdk:8-jre-alpine
LABEL maintainer="Daniel Gresak (daniel.gresak@ucdconnect.ie)"
COPY target/youbear-0.0.1-jar-with-dependencies.jar /youbear-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/youbear-0.0.1-jar-with-dependencies.jar", "lf.fleet.youbear.Main"]