# Smart-Vehicle (Exposed-Thing)

Author: Jörg Striebel

This TypeScript code implements the base of our smart vehicle thing, containing the logic of the so-called Exposed-Thing. Unlike in our first attempt (see directory “old_scripts”) were we implemented an Exposed-Thing and a Consumed-Thing directly in JavaScript, the approach of using TypeScript provides not only type safety but more importantly allows the separation of source code and build directories. Moreover, by using node-wot merely as a npm dependency enables us to only install the dependencies required for this specific use case. For instance, in our case we have only installed the HTTP binding while omitting all other dependencies such as CoAP, MQTT, etc.

This smart vehicle provides the following so-called Property Affordances, Action Affordances, and Event Affordances:

Properties:
-	propFleetId
-	propVehicleId
-	propOilLevel
-	propTyrePressure
-	propTotalMileage
-	propServiceDistance
-	propDoorStatus
-	propMaintenanceNeeded

Actions:
-	actionLockDoor
-	actionUnlockDoor

Events:
-	eventLowOnOil
-	eventLowTyrePressure
-	eventMaintenanceNeeded

All these affordances are defined in the Thing Description (TD) which is embedded in this vehicle. To maintain reusability, certain properties can be injected via the constructor from the starting point (index.js) which can be seen as the index.html of websites.
Depending on the property, they may possess all or only a subset of the following attributes [readable, writable, observable]. 

For demonstration purposes, the smart-vehicle Exposed-Thing implementation also contains an emulation which emulates the mileage increase, the oil consumption, and the tyre pressure loss over time. Whenever a critical threshold is reached, the emulation then triggers the events accordingly. 

## Code structure:

-	**index.js**:  The index.js can be seen the starting point of the Exposed-Thing, similarly to the index.html of websites. This is common practice for npm packages.
-	**src**: This directory contains the logic of the Exposed-Thing in TypeScript
-	**dist**: This directory contains the trans-compiled logic of the Exposed-Thing in JavaScript source format, which is then invoked by the index.js.
-	**package.json**: Contains all dependencies for the npm project.


## Useful commands at a glance:
- npm run clean
- npm install
- npm run build
- npm run buildAll
- npm run start

## Retrieving the Thing Description

GET http://\<hostname\>:\<PORT\>/\<title\>
 
It returns the Thing Description (TD) in JSON, describing all Affordances Interactions, metadata, etc. of the Thing.
Note that the endpoint "smart-vehicle" is defined in the TD as the title.


## Reading a Property Value

GET http://\<hostname\>:\<PORT\>/\<title\>/properties/\<property-name\>

For example, if we want to read the property 'propOilLevel' we would have an URI that looks like this:

GET http://\<hostname\>:\<PORT\>/\<title\>/properties/propOilLevel

It returns a JSON object containing all available resources. 


## Writing a Property Value

For example, if we want to reset the "propMaintenanceNeeded" property to false then the URI looks like this:

PUT http://\<hostname\>:\<PORT\>/\<title\>/properties/propMaintenanceNeeded?=false

Payload format: JSON, datatype: Integer

After updating the values successfully, the exposed thing responds with "204 No Content".


## Executing an Action

### Action: actionLockDoor

The action "lockDoor" locks the vehicle door and returns the status "LOCKED" along with "200 OK" on success.

POST http://\<hostname\>:\<PORT\>/\<title\>/actions/actionLockDoor


### Action: actionUnlockDoor

The action "unlockDoor" unlocks the vehicle door and returns the status "UNLOCKED" along with "200 OK" on success.

POST http://\<hostname\>:\<PORT\>/\<title\>/actions/actionUnlockDoor


# Docker

### Creating a Docker network

In this example, we create a Docker network, where the name of the network is "leetnetwork".

* docker network create leetnetwork
### Listing all Docker networks

* docker network ls

### Building a Docker image

In this example, we create a Docker image where "leetfleet" is the namespace, "vehicle" the image name, and as a tag "latest" will be automatically defined unless specified otherwise.

* docker build . -t vehicle

### Creating and running a vehicle container

In this example, we define the name "vehicle1" as the container name.

* docker run --rm --name vehicle1 --network-alias vehicle1 --network="leetnetwork" vehicle:latest

### Inspecting on which network a container is on

In this example, we want to inpsect on which network the container  "triple-store-wothive-1" is on.

* docker inspect triple-store-wothive-1 -f "{{json .NetworkSettings.Networks }}"



