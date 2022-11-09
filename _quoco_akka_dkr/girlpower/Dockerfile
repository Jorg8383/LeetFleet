FROM openjdk:8-jre-alpine
LABEL maintainer="Tomás Kelly (tomas.kelly1@ucdconnect.ie)"
COPY target/girlpower-0.0.1-jar-with-dependencies.jar /girlpower-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/girlpower-0.0.1-jar-with-dependencies.jar","service.girlpower.Main","-b","broker"]