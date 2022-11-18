FROM openjdk:8-jre-alpine
LABEL maintainer="Daniel Gresak (daniel.gresak@ucdconnect.ie)"
COPY target/web_portal-0.0.1-jar-with-dependencies.jar /web_portal-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/web_portal-0.0.1-jar-with-dependencies.jar", "lf.http.HttpToAkka"]