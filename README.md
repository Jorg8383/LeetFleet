# LeetFleet - A Web of Things Fleet Management System

## Description

LeetFleet Management System is an infrustructure/proof-of-concept specialised in vehicle management. This solution can be used for the management of many fleet types such as delivery fleets, taxi fleets or general company car fleet management. The architecture has been designed to store and process general road vehicle information but can be easily customised to accomodate more specific fleet needs.

As it currently stands, we store and process such information as:

- Vehicle ID
- Fleet ID
- Tyre Pressure
- Mileage
- Next distance for service
- Door Status (LOCKED or UNLOCKED)
- Maintenance Needed (TRUE or FALSE)

Each vehicle belongs to a certain fleet, and the way information is processed can be customised to a particular fleets specifications making this solution highly flexible to your business needs.

Our client web page allows fleet managers to view all the information outlined above for each of their vehicles as well as remotely locking or unlocking the vehicle doors. Other remote functionality could be added at a future stage.

The behaviour of smart vehicles, which are called Exposed Things, is simulated within the vehicle application logic. These vehicles communicate with the fleet management system via the cutting-edge technology standard Web of Things by utilising so-called interaction affordances to read/write properties, invoke actions and subscribe/listen to events.

The main technologies used within the application includes but is not limited to:

- Akka (Typed; Java implementation)
- Web of Things (Node.js implementation)
- Django (client application)

## Getting started and Installation

### Requirements

We have used Maven and Docker to make the installation and usage of the application as easy as possible.

The first step is to make sure you have Maven installed on your system. Also ensure that Docker and Docker Desktop is installed and open.

### Clone and Installation

```
git clone https://gitlab.com/comp30220/2022/leetfleet.git
```

Then ensure you are in the root folder and run the following:

```
mvn clean compile install
```

This will compile all the Java projects and install all the Docker images locally on your system with the appropriate names.

### Running of Application

Once this is complete, you have two options for running the system using docker-compose which are:

```
docker compose up
```

-OR-

```
docker compose -f dkr-comp1-wot-dir-service.yml up
```

```
docker compose -f dkr-comp2-wot-akka.yml up
```

We reccomend using the second option if you would like cleaner logs during debugging or would like an overview of what's happening in the background using the logs. This seperates the directory service logs and the main Akka and WoT logs.

The first option works the same way but can be more difficult to follow when debugging using the logs but works well if you just want to run or deploy the application.

## Usage

### Client

The application takes some time for each component to start up, once this has started you can access the client page at `http://localhost/`.

You will be greeted with the a header, and a drop down menu that should contain a list of the fleet managers.

Once you click on a fleet manager, you will be presented with the vehicles managed by the selected manager. You will be able to view all the properties of the vehicles. You can also click on the button at the bottom of each description and this will present you with just that vehicle and a button which will allow you to lock or unlock the vehicle.

If you'd like to return to the main menu, there is a back button at the top right of the screen or you can click on the "Leetfleet" header which will also bring you back.

![Client Changing WoT Property](/_resources/client-clip.gif "Client Changing WoT Property"){width=75%}

### Redis

You can view the information stored in Redis by accessing `http://localhost:8001/`. This will present to you the information which is stored from our lf services backend application (Akka). Here you can confirm that the information recieved by the client application matches the information held be akka if you wish.

![Redis](/_resources/redis.png "Redis"){width=75%}

### Web of Things Thing

You can use Postman to directly access a Thing description or an individual property of a Thing. Each Thing has been port forwarded to localhost with their own port number which can be found in the docker-compose file.

#### Retrieving a Thing Description

The examples given below refer to a vehicle which belongs to the careless fleet manager.

The Thing description can be accessed with a GET request to `http://<hostname>:<PORT>/<title>`.

The title is made up of the prefix "wot-id-mfr-vin-" + the vehicle ID which is defined in the docker-compose files as ENV_VEHICLE_NUMBER and the port is forwarded within the docker-compose file. The port is definited in the port forwarding statement.

See image below for the details of where the vehicle ID and port are configured (highlighted in grey). This can be found in the docker-compose.yml file within the root directory.

![docker-compose port and vehicle number](/_resources/readme_docker_compose.png "docker-compose port and vehicle number"){width=50%}

For example: `http://localhost:8100/wot-id-mfr-vin-0001`

This smart vehicle provides the following so-called Property Affordances, Action Affordances, and Event Affordances:

Properties:

- propFleetId
- propVehicleId
- propOilLevel
- propTyrePressure
- propTotalMileage
- propServiceDistance
- propDoorStatus
- propMaintenanceNeeded

Actions:

- actionLockDoor
- actionUnlockDoor

Events:

- eventLowOnOil
- eventLowTyrePressure
- eventMaintenanceNeeded

All these affordances are defined in the Thing Description (TD) which is embedded in the vehicle application logic.

![Postman](/_resources/postman-td.png "Postman"){width=50%}

#### Reading a Property

To read a property you can make a GET request to `http://<hostname>:<PORT>/<title>/properties/<property-name>`.

For example, if we want to read the property 'propOilLevel' we would make a GET request to the following URI: `http://localhost:8100/wot-id-mfr-vin-0001/properties/propOilLevel`

![Postman](/_resources/postman-get.png "Postman"){width=50%}

#### Writing a Property

To write a property, provided its writeable, you can make a PUT request to `http://<hostname>:<PORT>/<title>/properties/<property-name>`.

For example, if we want to change the property "propFleetId" we would make a PUT request to the following URI `http://localhost:8100/wot-id-mfr-vin-0001/properties/propFleetId` with a JSON payload containing the value to set.

![Postman](/_resources/postman-put.png "Postman"){width=50%}

#### Invoking an Action

To invoke an action on a smart vehicle we can perform a POST request to `http://<hostname>:<PORT>/<title>/actions/<action-name>`.

For example, if we want to lock the door of the vehicle we woud make a POST request to the following URI `http://localhost:8100/wot-id-mfr-vin-0001/actions/actionLockDoor`.

![Postman](/_resources/postman-post.png "Postman"){width=50%}

## Building and Running Individual Sections of Application

### Builing Individual Services withing lf_services

Anything within the lf_services directory are Akka services and can be built by using the following command within the desired subdirectory:

```
mvn clean compile install
```

Anything outide of lf_services can be built individually by following sections below

### Building and Running Individual Docker Images

If you would like to install a particular docker image, you can first ensure you're in the correct directory for that service. Then run:

```
docker build . -t <name of image>
```

Afterwards you can run the image by using the following command:

```
docker run <name of image>
```

Be aware that the docker-compose file has particular naming conventions for the images being built. These can be found within the docker-compose file

### WoT build with NPM

The directories that contain the Javascript code for running the Web of Things (WoT) architectural system for emulating vehicles and communicating with the backend system require separate builds and installation.
To simply run the project, run the following command in your terminal from the "vehicle" and the "wot_bridge_directory" directories:

```
docker build -t <directory-name> .
```

The naming convention is very important here i.e. when building the vehicle directory's docker image, the command should read as follows:

```
docker build -t 'wot_bridge_directory' .
```

This naming convention is based on the default hostname assignment within a docker-compose network and incorrectly naming these docker images could break the communication between elements of the system

## Roadmap

Additional functionality which would be nice to add at a later stage includes suggestions such as:

- Allowing a fleet manager to log that a car has been serviced, which will reset the next distance for service flag via the client webpage
- Allowing a fleet manager to log if maintenance was carried out via the client webpage.
- Notifications via email/sms to a fleet manager if vehicle needs mainanence or the vehicle is due a service.
- Login credentials for the client webpage to access the associated fleetmanager information in the backend
- Defining various vehicle types/sizes to better exploit the Web of Things discovery mechanism by searching for a specific vehicle type/size.

## Project Status and Contributing

This project was developed to fulfill assignment requirements for a Master in
Computer Science module (COMP41720 Distributed Systems) in UCD.

Development has stopped. Should someone choose to fork this project or volunteer
to step in as a maintainer or owner, we would be happy to discuss it.

For more information on the technologies used, See documentation linked below.

Akka Documentation
https://akka.io/docs/

WoT Documentation:
https://www.w3.org/WoT/

## Authors and acknowledgment

We would like to thank Dr. Rem Collier for his ongoing advice and support prior and during this projects completion. We have also benefited greatly from ongoing support from the modules Teaching Assistant and Demonstrators.

Authors of the project include:

- Tomás Kelly
- Jörg Striebel
- Daniel Gresak (daniel.gresak@ucdconnect.ie)
- Ian Foster

We would also like to thank the creators of the Redis Stack docker image used within this application. This is the [link](https://hub.docker.com/r/redis/redis-stack) to the Docker Hub page this was found.

We also would like to thank the creators of the wot-hive which was used in this project, the original git repository is available [here](https://github.com/oeg-upm/wot-hive)

## License

GNU GPL v3

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
