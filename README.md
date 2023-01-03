## Suggestions for a good README

Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.

# LeetFleet WoT Fleet Management System

## Description

<!-- Let people know what your project can do specifically. Provide context and add a link to any reference visitors might be unfamiliar with. A list of Features or a Background subsection can also be added here. If there are alternatives to your project, this is a good place to list differentiating factors. -->

LeetFleet WoT Fleet Management System is software specialised in vehicle management. This solution can be used for the management of many fleet types such as delivery fleets, taxi fleets or general company car fleet management. The architecture has been designed to store and process general road vehicle information but can be easily customisable to accomodate more specific fleet needs.

As it currently stands, we store and process such information as:

1. Vehicle ID
2. Fleet ID
3. Tyre Pressure
4. Mileage
5. Next distance for service
6. Door Status (LOCKED or UNLOCKED)
7. Maintenance Needed (TRUE or FALSE)

Each vehicle belongs to a certain fleet, and the way information is processed can be customised to a particular fleets specifications making this solution highly flexible to your business needs.

Our client web page allows fleet managers to view all the information outlined above for each of their vehicles as well as remotely locking or unlocking the vehicle doors. Other remote functionality could be added at a future stage.

The main technology used within the application includes but is not limited to:

1. Akka (Typed; Java implementation)
2. Web of Things (Node.js implementation)
3. Django (client application)

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

Akka Documentation
https://akka.io/docs/

WoT Documentation:
https://www.w3.org/WoT/

<!-- ## Test and Deploy

Use the built-in continuous integration in GitLab.

- [ ] [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/index.html)
- [ ] [Analyze your code for known vulnerabilities with Static Application Security Testing(SAST)](https://docs.gitlab.com/ee/user/application_security/sast/)
- [ ] [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/ee/topics/autodevops/requirements.html)
- [ ] [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/ee/user/clusters/agent/)
- [ ] [Set up protected environments](https://docs.gitlab.com/ee/ci/environments/protected_environments.html)

---

## Visuals

Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method. -->

<!-- ## Installation

Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.
 -->

## Usage

<!-- Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README. -->

### Client

The application takes some time for each component to start up, once this has started you can access the client page at `http://localhost/`.

You will be greeted with the a header, and a drop down menu that should contain a list of the fleet managers.

Once you click on a fleet manager, you will be presented with the vehicles managed by the selected manager. You will be able to view all the properties of the vehicles. You can also click on the button at the bottom of each description and this will present you with just that vehicle and a button which will allow you to lock or unlock the vehicle.

If you'd like to return to the main menu, there is a back button at the top right of the screen or you can click on the "Leetfleet" header which will also bring you back.

![Client Changing WoT Property](/_resources/client-clip.gif "Client Changing WoT Property"){width=75%}

### Redis

You can view the information stored in Redis by accessing `http://localhost:8001/`. This will present to you the information which is stored from our lf services backend application (Akka). Here you can confirm that the information recieved by the client application matches the information held be akka if you wish.

### Web of Things Thing

You can use Postman to directly access a Thing description or an individual property of a Thing. Each Thing has been port forwarded to localhost with their own port number which can be found in the docker-compose file.

#### Retrieving a Thing Description

To give an example, there is a careless vehicle being forwarded to port 8100 in localhost.

The Thing description can be accessed with a GET request to `http://localhost:<port for vehicle>/\<vehicle id>/`.

The vehicle ID is "WoT-ID-Mfr-VIN" + the ENV_VEHICLE_NUMBER which can also be found in the docker-compose file

In this case, you can access the Thing Description from `http://localhost:8100\WoT-ID-Mfr-VIN0001/`.

This will give you all the properties available to view and change.

#### Reading a Property

To view a property you can make a GET request to `http://localhost:<port for vehicle>/\<vehicle id>/properties/\<property name>/`.

For the example above, you can make a GET request to `http://localhost:8100\WoT-ID-Mfr-VIN0001/properties/propDoorStatus` which will return the door status of that vehicle.

#### Changing a Property

You can also directly change a property by doing a PUT request to the same endpoint above. You should use the JSON datatype and include a string with the new value you want for the property. So in the case above you could include a string with word "LOCKED". When this is successful, you will recieve a 204 response.

## Building and Running Individual Sections of Application

### Building and running Docker images

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

`docker build -t <directory-name> .`

The naming convention is very important here i.e. when building the vehicle directory's docker image, the command should read as follows:

`docker build -t 'wot_bridge_directory' .`

This naming convention is based on the default hostname assignment within a docker-compose network and incorrectly naming these docker images could break the communication between elements of the system

<!-- 1. `docker network create leet-fleet`
2. `docker run --network-alias core --network leet-fleet -p 80:8080 core:latest`
3. In a new terminal; ``
4. In a new terminal; ``
5. In a new terminal; ``
6. (If your PC isn't fast - allow the services a few seconds to start!)<br>
   In a new terminal; ``<br> -->

<!-- ## Running Docker Compose

`docker compose up`

-OR-

`docker compose -f dkr-comp1-wot-dir-service.yml up`
`docker compose -f dkr-comp2-wot-akka.yml up` -->

## Roadmap

ideas for releases in the future, it is a good idea to list them in the README.
WoT Items
Akka Items
Client Items

Additional functionality which would be nice to add at a later stage includes suggestions such as:

1. Allowing a fleet manager to log that a car has been serviced, which will reset the Next distance for service flag via the client webpage
2. Allowing a fleet manager to log if maintenance was carried out via the client webpage.
3. Notifications via email/sms to a fleet manager if vehicle needs mainanence or the vehicle is due a service.
4. Login credentials for the client webpage to access the associated fleetmanager information in the backend

## Contributing

<!-- State if you are open to contributions and what your requirements are for accepting them. -->

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.

## Authors and acknowledgment

We would like to thank Dr. Rem Collier for his ongoing advice and support prior and during this projects completion. We have also benefited greatly from ongoing support from the modules Teaching Assistant and Demonstrators.

Authors of the project include:

- Tomas Kelly
- Jorg
- Daniel Gresak (daniel.gresak@ucdconnect.ie)
- Ian Foster

Should we acknowled Node-Wot
Akka

## License

GNU GPL v3

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## Project status

This project was developed to fulfill assignment requirements for a Master in
Computer Science (Converstion) module (COMP41720 Distributed Systems) in UCD.

Development has stopped. Should someone choose to fork this project or volunteer
to step in as a maintainer or owner, we would be happy to discuss it.
