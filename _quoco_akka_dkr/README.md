# Introduction

This is the AKKA implementation of the COMP41720 Quotation Service, for Lab6

## Tom's Fork of the COMP41720 Quotation Service

The fork of the QuoCo Quotation service I used to serve as a start point for the
various COMP41720 labs has the following mods from the original:
* Project split into Maven multi-module project
* All tabs replaced with spaces
* Redundant "implements QuotationService" removed from three Quotation Services
* Quotation and ClientInfo data classes implement the java.io.Serializable interface
* All references to basic registry service removed

# Running the Program

This is a maven project, supplied as a .zip archive

1.  Download/Unzip the project
2.  Use a command shell, and go to the quoco-akka folder
3.  Type: `mvn clean compile install'

This will prepare the require packages. There are no docker images.

## Docker Images

## Running the Docker Images Directly

1. `docker network create quoco_net`
2. `docker run --network-alias broker --network quoco_net broker:latest`
3. In a new terminal; `docker run --network-alias auldfellas --network quoco_net auldfellas:latest`
4. In a new terminal; `docker run --network-alias dodgydrivers --network quoco_net dodgydrivers:latest`
5. In a new terminal; `docker run --network-alias girlpower --network quoco_net girlpower:latest`
6. (If your PC isn't fast - allow the services a few seconds to start!)<br>
   In a new terminal; `docker run --network-alias client --network quoco_net client:latest`<br>

The broker and all quotation services are now running in their containers, with
port localhost:8080 mapped to the docker network the containers share.
Running the client locally (mvn spring-boot:run -pl client) will retrieve a set of
quotes.

## Running the Docker Images Using Docker Compose

1. `docker compose up`
2. (If your PC isn't fast - allow the containers/services a few seconds to start!)
   Sit back and watch the magic happen.

# Further Information
* This project was build and tested using java 1.8.
* log4j2 is used for logging. All log appenders are set to "info"