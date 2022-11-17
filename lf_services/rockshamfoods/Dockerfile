FROM openjdk:8-jre-alpine
LABEL maintainer="Daniel Gresak (daniel.gresak@ucdconnect.ie)"
COPY target/rockshamfoods-0.0.1-jar-with-dependencies.jar /rockshamfoods-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/rockshamfoods-0.0.1-jar-with-dependencies.jar", "lf.fleet.rockshamfoods.Main"]