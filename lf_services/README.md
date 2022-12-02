# LeetFleet (lf_services)

## Startup Sequence
To create required docker network: docker network create leetfleet_net
To run the whole shooting match: docker compose up

### Compose Start:
By far the easiest method. 'docker compose up' will start an entire configured
akka cluster.
### Individual Start:
1) Dummy Cloud Storage
   There are lots of arguments for REDIS. Easiest approach is to run:
    - docker compose -f docker-comp-redis.yml up
    ...to run redis on it's own
    - docker compose up
    ... to run docker-compose.yml and run redis as part of the leetfleet ecosystem.

2) Registry
   NOTE: `registry` is **THE FIRST** Cluster Seed Node. It MUST start first for the Cluster to function
   No real point in running locally, unless debugging, but:
   mvn exec:java -Dexec.args="-a localhost" -pl registry
   ... or...
   docker run --network-alias registry --network leetfleet_net registry:latest

3) Fleet Managers (all - simply insert chosen manager name)
   - careless
   - fastidious
   - fleetless
   - paranoid
   NOTE: `registry` or `webportal` MUST be running (Cluster Seed Nodes)
   No real point in running locally, unless debugging, but:
   mvn exec:java -Dexec.args="-a localhost" -pl careless
   ... or...
   docker run --network-alias careless --network leetfleet_net careless:latest

4) webportal (HttpToAkka) registers with registry
   Running locally:
   mvn exec:java -Dexec.args="-a localhost" -pl webportal
   Running Docker image:
   docker run -p 8080:8080 --network-alias webportal --network leetfleet_net webportal:latest


# Ports for LeetFleet System
Redis         : 6379
RedisInsight  : 8001
# HTTP Server
WebPortal     : 8080
# Akka Cluster
Registry  : 2550 (*** AKKA Seed Node, at least one Seed Node must be started for Cluster to Function)
WebPortal : 2551 (*** AKKA Seed Node, at least one Seed Node must be started for Cluster to Function)
Careless  : 2552
Fastidious: 2553
Fleetless : 2554
Paranoid  : 2555



) web portal gets message
ii) Tells message to registry/router actor (message has to have a car id)
iii) registry sends to all fleetmanagers
iv) all but ignores the message
v) the fleet the vhicle belongs to ... does the thing...
   -> create a car actor
   -> store shit in the database

Vehicle Event will send the translated message to the vehicle
The Vehicle will update it's model and store it!??


TODO:
========
EndPoints to be Be Implemented in AKKA:
Client:
List all fleets (/web/list_fleets)
Given a fleet - list all vehicles for fleet (/web/list_vehicles?fleet_id=sssssss)
Given a Car - show vehicle properies (/web/show_vehicle?vehicle_id=sssssss)
Set a property for Car (lock doors) (/web/lock_doors?vehicle_id=sssssss)

Vehicle (WoT) Interface
Initial large blob of data????
- We're hoping to get at a minimum a URI we can call to set a fleet id?  On exposed thing directly
- Actor somewhere that waits for a message to tell it to send a fleet_id to a vehicle (hard code the logic here)

totalMileage (/wot/total_mileage?vehicle_id=sssssss&fleet_id=ssssss)
nextServiceDistance (/wot/next_service_distance?vehicle_id=sssssss&fleet_id=ssssss
doorStatus (/wot/door_status?vehicle_id=sssssss&fleet_id=ssssss
maintenanceNeeded (/wot/maintenance_needed?vehicle_id=sssssss&fleet_id=ssssss

lockDoor (/wot/lock_door?vehicle_id=sssssss&fleet_id=ssssss)
unlockDoor (/wot/unlock_door?vehicle_id=sssssss&fleet_id=ssssss)

eventLowOnOil (/wot/low_on_oil?vehicle_id=sssssss&fleet_id=ssssss)
eventLowTyrePressure (/wot/low_tire_pressure?vehicle_id=sssssss&fleet_id=ssssss)
eventMaintenanceNeeded (/wot/maintenance_need?vehicle_id=sssssss&fleet_id=ssssss)