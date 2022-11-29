- npm install
- npm run build
- npm run start

# Smart-Vehicle

To run the servient of the smart-vehicle run:
node smart_vehicle.js

The HTTP server runs locally by default on port 8080. This port can be changed.

The CRUD operations below were tested using Postman, accessing the HTTP server on localhost, port 8080.
However, taking a closer look at the Thing Description (TD) reveals the actual IP address of the endpoints to interact with the exposed Thing is obviously different.

## Retrieving the Thing Description
GET http://localhost:8080/smart-vehicle
 
It returns the Thing Description (TD) in JSON, describing all Affordances Interactions, metadata, etc. of the Thing.
Note that the endpoint "smart-vehicle" is defined in the TD as the title.

## Properties

### Property: allAvailableResources
GET http://localhost:8080/smart-vehicle/properties/allAvailableResources

This property can be used to retrieve all availabe resources. For now, we have only two sensor values: "oilLevel" and "tyrePressure".
It returns a JSON object containing all available resources. 

### Property: availableResourceLevel
The "availableResourceLevel" property enables to read and write an indivudal resource. For example, reading/writing the "tyrePressure" property.


**Example - writing the a new value for "tyrePressure":**

PUT http://localhost:8080/smart-vehicle/properties/availableResourceLevel?id=tyrePressure

Payload format: JSON, datatype: Integer

After updating the values successfully, the exposed thing responds with "204 No Content".


**Example - reading the value for "tyrePressure":**

GET http://localhost:8080/smart-vehicle/properties/availableResourceLevel?id=tyrePressure


### Property: totalMileage

The property "totalMileage" is read-only, and is internally increased using a random function every time the property is being read.

GET http://localhost:8080/smart-vehicle/properties/totalMileage


### Property: nextServiceMileage

The "nextServiceMileage" property is by default set to 30,000 km. This property is read-/writeable. 
Currently it is decreased by the same amount the "totalMileage" is increased.
However, this emulation might be improved and enhanced in the future...

GET http://localhost:8080/smart-vehicle/properties/nextServiceMileage

PUT http://localhost:8080/smart-vehicle/properties/nextServiceMileage

The payload is JSON and the value is an integer. It also responds with "204 No Content" on success.


### Property: doorStatus

The "doorStatus" property indicates whether the door is "LOCKED" or "UNLOCKED" and returns the status as a string.

GET http://localhost:8080/smart-vehicle/properties/doorStatus


### Property: maintenanceNeeded

The "maintencanceNedded" property is set TRUE when the property "nextServiceMileage" is lower than 500 km.
This property is read-/writeable and observable.

GET http://localhost:8080/smart-vehicle/properties/maintenanceNeeded

PUT http://localhost:8080/smart-vehicle/properties/maintenanceNeeded

The payload is JSON and the value is an boolean. It also responds with "204 No Content" on success.


## Actions

### Action: lockDoor

The action "lockDoor" locks the vehicle door and returns the status "LOCKED" along with "200 OK" on success.

POST http://localhost:8080/smart-vehicle/actions/lockDoor


### Action: unLockDoor

The action "unlockDoor" unlocks the vehicle door and returns the status "UNLOCKED" along with "200 OK" on success.

POST http://localhost:8080/smart-vehicle/actions/unLockDoor

