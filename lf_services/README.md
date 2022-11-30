# LeetFleet

1) registry/router (broker) (good name?) (maybe even the first actor???)

2) web_portal (HttpToAkka) registers with registry
3) fleet managers register with registry (x3)
    -> careless
    -> fastidious
    -> paranoid
    -> fleetless

i
# Ports for LeetFleet System
Redis         : 6379
RedisInsight  : 8001
# HTTP Server
WebPortal     : 8080
# Akka Cluster
Registry  : 2550
WebPortal : 2551
Careless  : 2552
Fastidious: 2553
Fleetless : 2554
Paranoid  : 2555


To Test the web portal (no docker) we can:
mvn exec:java -Dexec.args="-a localhost" -pl web_portal



FleetManagers will load supported id's from redis




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