# The WoT-Bridge, the WoT-Hive Directory and Exposed-Things

Authors: Jörg Striebel & Ian Foster

## Code structure:

-	**index.js:**  The index.js contains the bridge logic which is responsible for retrieving Thing-Descriptons (TD) of all registered Exposed-Things and spinning up so-called "Consumed-Things" for each of them. 
-	**src:** This directory contains the logic of the Consumed-Thing in TypeScript. The Consumed-Thing communicates with the Web-Portal via HTTP.
-	**dist:** This directory contains the trans-compiled logic of the Consumed-Thing in JavaScript source format, which is then invoked by the index.js.
-	**Package.json:** Contains all dependencies for the npm project.

## The WoT-Hive directory & the ideal design solution

The WoT-Hive, which is an implementation of a W3C Web of Things directory, forms the backbone of our bridge between the Web of Things world and the backend AKKA part. It allows for Exposed-Things to register themselves with this directory and for the bridge to retrieve the Thing Descriptions (TD) of these Exposed-Things either individually or as a list of TDs. The WoT-Hive API offers CRUD methods to modify its configuration, register Exposed-Things, retrieve individual or the entire list of registered TDs, and to subscribe to so-called Server-Sent-Events (SSE). The SSE would allow to notify a client which has subscribed to specific events, whenever one of these events is triggered. For instance, a client that has registered itself for a “thing created” event, would be notified every time a new Exposed-Things has been registered. So far, that is the theory.

## The problem

However, in practise it turned out that the SSE API had not been fully functional at the time of testing it end of November 2022. This WoT-Hive project was launched at a university in Madrid and released in early 2022. Judging by the commit history this project still seemed to be in progress, potentially not yet fully implemented but certainly lacking comprehensive documentation. So, when trying to subscribe to the “created” event, the server would return with a HTTP 404 error, indicating that unlike outlined in the description of the WoT-Hive API, this feature may had not been implemented yet. The code for the SSE client implementation can be found commented out at the end of this file.

## The workaround

Consequently, we had to come up with a workaround. Instead of having the bridge notified through Server-Sent-Events every time a new Exposed-Thing is registered, the bridge is now sending requests to the server at regular intervals (polling) for retrieving the entire list of registered TDs. In theory it would be possible to define an expiry date for registered TDs. However, since we are polling the directory now actively, we need to ensure that only currently Exposed-Things are registered with the directory as only those can be consumed by its counterpart called “Consumed-Thing”. 

## The current bridge implementation

To realise this, the functionality of the bridge can be summarised as follows:
* Fetching a list of TDs from the WoT-Hive directory at regular intervals (currently every 10 sec)
* Checking the fetched cache for relevant TD entries
    * Keeping track of outdated TDs to be deleted
    * Keeping track of current TDs to be consumed
    * Keeping track of TDs that have already be consumed
* Cleaning up the WoT-Hive directory by deleting outdated TDs. Therefore, keeping the amount of data that must be exchanged to a minimum
* Creating a so-called “Consumed-Thing”
    * For each TD to be consumed a so-called “Servient” is instantiated
    * For each TD to be consumed a HTTP server with its individual port is being created
    * A client-factory and the HTTP server is than added to the servient
* Finally, the servient spins up a Consumed-Thing for each TD
 

For more information about the WoT-Hive directory, check out:
https://github.com/oeg-upm/wot-hive

# Docker

### Creating a Docker network

Unless already created, this is an example on how to create a Docker network, where the name of the network is "leetnetwork".

* docker network create leetnetwork
### Listing all Docker networks

* docker network ls

### Building a Docker image of the WoT-Bridge

In this example, we create a Docker image where "leetfleet" is the namespace, "wot_bridge" the image name, and as a tag "latest" will be automatically defined unless specified otherwise.

* docker build . -t leetfleet/wot_bridge

### Creating and running a vehicle container

In this example, we define the name "wot_bridge" as the container name.

* docker run --rm --name wot_bridge --network-alias wot_bridge --network="leetnetwork" leetfleet/wot_bridge:latest