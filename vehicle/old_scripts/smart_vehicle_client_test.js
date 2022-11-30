// client.js
// Required steps to create a servient for a client
const { Servient, Helpers } = require("@node-wot/core");
const { HttpClientFactory } = require('@node-wot/binding-http');

const servient = new Servient();
servient.addClientFactory(new HttpClientFactory(null));
const WoTHelpers = new Helpers(servient);

WoTHelpers.fetch("http://localhost:8080/smart-vehicle").then(async (td) => {
    try {
        servient.start().then(async (WoT) => {
            // Here we're conusming the thing
            try {
                const thing = await WoT.consume(td);
                log("Thing Description:", td);

                // // Read property allAvailableResources
                // let propFleetId = await (await thing.readProperty("propFleetId")).value();
                // log("propFleetId value is:", propFleetId);

                // // Let's change the oil level to 90%
                // await thing.writeProperty("availableResourceLevel", 90, { uriVariables: { id: "oilLevel" } });
                // // Now let's check if the oil level has changed
                // const oilLevel = await (
                //     await thing.readProperty("availableResourceLevel", { uriVariables: { id: "oilLevel" } })
                // ).value();
                // log("oilLevel value has changed to:", oilLevel);
                // // Alternatively, this change can also be seen in allAvailableResources property
                // allAvailableResources = await (await thing.readProperty("allAvailableResources")).value();
                // log("allAvailableResources value after the change is:", allAvailableResources);

                // // Let's read the total mileage and distance till next service is due
                // let totalMileage = await (await thing.readProperty("totalMileage")).value();
                // log("Total mileage:", totalMileage);
                // let nextServiceDistance = await (await thing.readProperty("nextServiceDistance")).value();
                // log("Distance until next service is due:", nextServiceDistance);

                // // Let's read the door status
                // let doorStatus = await (await thing.readProperty("doorStatus")).value();
                // log("Reading doorStatus before invoking the 'lockDoor' action:", doorStatus);
                // // Now let's invoke an action to change the door status
                // let lockDoor = await thing.invokeAction("lockDoor");
                // let lockDoorFeedback = await lockDoor.value();
                // log("Return value after invoking the action 'lockDoor':", lockDoorFeedback);

                // // It's also possible to set a client-side handler for observable properties
                // thing.observeProperty("maintenanceNeeded", async (data) => {
                //     log("Observed 'maintenanceNeeded' property has changed! New value is:", await data.value());
                // });
                // thing.observeProperty("totalMileage", async (data) => {
                //     log("Observed 'totalMileage' property has changed! New value is:", await data.value());
                // });
                // thing.observeProperty("nextServiceDistance", async (data) => {
                //     log("Observed 'nextServiceDistance' property has changed! New value is:", await data.value());
                // });

                // // Let's set up a handler for each event
                // thing.subscribeEvent("eventLowOnOil", async (data) => {
                //     // For now let's simply log the message when the event is emitted
                //     log("eventLowOnOil:", await data.value());
                // });
                // thing.subscribeEvent("eventLowTyrePressure", async (data) => {
                //     // For now let's simply log the message when the event is emitted
                //     log("eventLowTyrePressure:", await data.value());
                // });
                // thing.subscribeEvent("eventMaintenanceNeeded", async (data) => {
                //     // For now let's simply log the message when the event is emitted
                //     log("eventMaintenanceNeeded:", await data.value());
                // });

            } catch (err) {
                console.error("Script error:", err);
            }        
        });
    }
    catch (err) {
        console.error("Script error:", err);
    }
}).catch((err) => { console.error("Fetch error:", err); });

// Print data and an accompanying message in a distinguishable way
function log(msg, data) {
    console.info("================================================================================");
    console.info(msg);
    console.dir(data);
}
