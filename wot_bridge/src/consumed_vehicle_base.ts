// import * as WoT from "wot-typescript-definitions";

// Required steps to create a servient for a client
const { Servient, Helpers } = require("@node-wot/core");
const { HttpClientFactory } = require('@node-wot/binding-http');

const servient = new Servient();
servient.addClientFactory(new HttpClientFactory(null));
const WoTHelpers = new Helpers(servient);

WoTHelpers.fetch("http://localhost:8080/smart-vehicle").then(async (td) => {
    try {
        servient.start().then(async (WoT) => {
            // Here we're consuming the thing
            try {
                const thing = await WoT.consume(td);
                console.log("Consumed thing: " + thing.getThingDescription().title);

                await thing.observeProperty("maintenanceNeeded", async (data) => {
                    console.log("Observed 'maintenanceNeeded' property has changed! New value is:", await data.value());
                });
                await thing.observeProperty("totalMileage", async (data) => {
                    console.log("Observed 'totalMileage' property has changed! New value is:", await data.value());
                });
                await thing.observeProperty("nextServiceDistance", async (data) => {
                    console.log("Observed 'nextServiceDistance' property has changed! New value is:", await data.value());
                });

                await thing.subscribeEvent("eventLowOnOil", async (data) => {
                    // For now let's simply log the message when the event is emitted
                    console.log("eventLowOnOil:", await data.value());
                });
                await thing.subscribeEvent("eventLowTyrePressure", async (data) => {
                    // For now let's simply log the message when the event is emitted
                    console.log("eventLowTyrePressure:", await data.value());
                });
                await thing.subscribeEvent("eventMaintenanceNeeded", async (data) => {
                    // For now let's simply log the message when the event is emitted
                    console.log("eventMaintenanceNeeded:", await data.value());
                });
            } catch (err) {
                console.error("Script error:", err);
            }
        });
    }
    catch (err) {
        console.error("Script error:", err);
    }
}).catch((err) => { console.error("Fetch error:", err); })

