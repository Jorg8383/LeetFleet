# The WoT-Bridge, the WoT-Hive Directory and Exposed-Things

Authors: Jörg Striebel & Ian Foster

## Code structure:

- **index.js:**  The index.js contains the bridge logic which is responsible for retrieving Thing-Descriptons (TD) of all registered Exposed-Things and spinning up so-called "Consumed-Things" for each of them.
- **src:** This directory contains the logic of the Consumed-Thing in TypeScript. The Consumed-Thing communicates with the Web-Portal via HTTP.
- **dist:** This directory contains the trans-compiled logic of the Consumed-Thing in JavaScript source format, which is then invoked by the index.js.
- **Package.json:** Contains all dependencies for the npm project.

## The WoT-Hive directory & the ideal design solution

The WoT-Hive, which is an implementation of a W3C Web of Things directory, forms the backbone of our bridge between the Web of Things world and the backend AKKA part. It allows for Exposed-Things to register themselves with this directory and for the bridge to retrieve the Thing Descriptions (TD) of these Exposed-Things either individually or as a list of TDs. The WoT-Hive API offers CRUD methods to modify its configuration, register Exposed-Things, retrieve individual or the entire list of registered TDs, and to subscribe to so-called Server-Sent-Events (SSE). The SSE would allow to notify a client which has subscribed to specific events, whenever one of these events is triggered. For instance, a client that has registered itself for a “thing created” event, would be notified every time a new Exposed-Things has been registered. So far, that is the theory.

## The problem

However, in practise it turned out that the SSE API had not been fully functional at the time of testing it end of November 2022. This WoT-Hive project was launched at a university in Madrid and released in early 2022. Judging by the commit history this project still seemed to be in progress, potentially not yet fully implemented but certainly lacking comprehensive documentation. So, when trying to subscribe to the “created” event, the server would return with a HTTP 404 error, indicating that unlike outlined in the description of the WoT-Hive API, this feature may had not been implemented yet. The code for the SSE client implementation can be found commented out at the end of this file.

## The workaround

Consequently, we had to come up with a workaround. Instead of having the bridge notified through Server-Sent-Events every time a new Exposed-Thing is registered, the bridge is now sending requests to the server at regular intervals (polling) for retrieving the entire list of registered TDs. In theory it would be possible to define an expiry date for registered TDs. However, since we are polling the directory now actively, we need to ensure that only currently Exposed-Things are registered with the directory as only those can be consumed by its counterpart called “Consumed-Thing”.

## The current bridge implementation

To realise this, the functionality of the bridge can be summarised as follows:
* Fetching a list of TDs from the WoT-Hive directory at regular intervals. This interval is set as an environment variable in the docker compose file that includes the vehicle emulations, which can be modified based on testing/demonstration needs
* Checking the fetched cache for relevant TD entries. A TD entry is considered relevant if the directory service was started before that vehicle registered with the directory.
    * Keeping track of outdated TDs to be deleted
    * Keeping track of current TDs to be consumed
    * Keeping track of TDs that have already be consumed
* Cleaning up the WoT-Hive directory by deleting outdated TDs. Therefore, keeping the amount of data that must be exchanged to a minimum
* Creating a so-called “Consumed-Thing”
    * For each TD to be consumed a so-called “Servient” is instantiated (a servient is like a server that also functions as a client simultaneously)
    * For each TD to be consumed a HTTP server with its individual port is being created
    * A client-factory and the HTTP server is than added to the servient
* Each consumed thing is technically given its own servient by the bridge, as can be seen in the individual ports assigned to each consumed thing


For more information about the WoT-Hive directory, check out:
https://github.com/oeg-upm/wot-hive

# Docker

### Docker network

The Akka service, bridge, vehicles and client are all contained on the same Docker network in this example. This network is specified as "leetnetwork" in the docker compose file, which was created as a named network for use in debugging the communications inside the Docker network. 

All vehicles are given a network alias in the Docker network that docker compose defaults to the service name i.e. the "dummy-vehicle" service would be given the network alias "dummy-vehicle-1". Given the predictability of this behaviour, the hostnames of the different containers inside the Docker network were hard-coded to ease communication between the elements.

### Building a Docker image of the WoT-Bridge

In this example, we create a Docker image where "leetfleet" is the namespace, "wot_bridge" the image name, and as a tag "latest" will be automatically defined unless specified otherwise.

* docker build . -t leetfleet/wot_bridge

### Creating and running the WoT-Bridge container

In this example, we define the name "wot_bridge" as the container name. We could also pass the "--build-arg ARG_FIRST_SERVER_PORT=8200" argument, where 8200 stands for an
arbitrary port number of the first HTTP-Server being created for each consumed-thing.
This port number is incremented by one for each consumed-thing that's being created.

* docker run --rm --name wot_bridge --network-alias wot_bridge --network="leetnetwork" leetfleet/wot_bridge:latest


Note that if the bridge is run without the directory service being able, the bridge will fail with an error based on the failed request to the directory.


### Building the Docker image independently

If the bridge is being built independently of other services, please use the following command:

* docker build -t 'wot_bridge_directory:latest' .

This syntax, particularly the tag name, are important as the docker-compose file is expecting an image labelled "wot_bridge_directory:latest", so labelling the image with a different name could lead to issues when running the bridge service concurrently with the other services within the docker compose file.
