FROM openjdk:8-jre-alpine
LABEL maintainer="Daniel Gresak (daniel.gresak@ucdconnect.ie)"
COPY target/car_actor-0.0.1-jar-with-dependencies.jar /car_actor-0.0.1-jar-with-dependencies.jar
CMD ["/usr/bin/java", "-cp", "/broker-0.0.1-jar-with-dependencies.jar", "service.broker.Main","-t","5"]