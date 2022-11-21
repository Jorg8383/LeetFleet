// server.js
// Required steps to create a servient for creating a thing
const Servient = require('@node-wot/core').Servient;
const HttpServer = require('@node-wot/binding-http').HttpServer;

const servient = new Servient();
servient.addServer(new HttpServer());

servient.start().then((WoT) => {
    let allAvailableResources;
    let totalMileage = 10;
    // Initialise the service interval with 30,000 km
    let nextServiceMileage = 30000;
    let maintenanceNeeded;
    // Initialise tyre pressure, oil level, and door status
    let tyrePressure = 35; // [PSI]
    let oilLevel = 100; // [%]
    let doorStatus = "UNLOCKED";
    // Status variables - used to emit corresponding events only once
    let tyrePressureIsLow = false;
    let oilLevelIsLow = false;
    let nextServiceIsDue = false;
    let maintenanceNeddedHistory = false;
    WoT.produce({
        title: "smart-vehicle",
        description: `A smart vehicle that connects with our fleet manager`,
        support: "https://github.com/eclipse/thingweb.node-wot/",
        properties: {
            allAvailableResources: {
                type: "object",
                description: `Current level of all available resources given as an integer percentage for each particular resource.
    The data is obtained from sensors but can be set manually via the availableResourceLevel property in case the sensors are broken.`,
                readOnly: true,
                properties: {
                    oilLevel: {
                        type: "integer",
                        minimum: 0,
                        maximum: 100,
                    },
                    tyrePressure: {
                        type: "integer",
                        minimum: 0,
                        maximum: 50,
                    },
                },
            },
            availableResourceLevel: {
                type: "number",
                description: `Current level of a particular resource. Requires resource id variable as uriVariables.
    The property can also be overridden, which also requires resource id as uriVariables.`,
                uriVariables: {
                    id: {
                        type: "string",
                        enum: ["oilLevel", "tyrePressure"],
                    },
                },
            },
            totalMileage: {
                type: "integer",
                description: `The total mileage of the vehicle.`,
                minimum: 0,
            },
            nextServiceMileage: {
                type: "integer",
                description: `Mileage counter for service intervals.`,
                minimum: 0,
                maximum: 30000,
            },
            doorStatus: {
                title: "Door status",
                type: "string",
                enum: ["LOCKED", "UNLOCKED"]
            },
            maintenanceNeeded: {
                type: "boolean",
                description: `Indicates when maintenance is needed. The property is observable. Automatically set to true if oil or tyre pressure is too low.`,
                observable: true,
            },
        },
        actions: {
            lockDoor: {
                description: `Lock the car door`,
                output: { type: "string" },
            },
            unlockDoor: {
                description: `Unlock the car door`,
                output: { type: "string" },
            },
        },
        events: {
            eventLowOnOil: {
                description: `Low on oil.`,
                data: {
                    type: "string",
                },
            },
            eventLowTyrePressure: {
                description: `Low tyre pressure.`,
                data: {
                    type: "string",
                },
            },
            eventMaintenanceNeeded: {
                description: `Maintenance needed.`,
                data: {
                    type: "string",
                },
            },
        },
    })
        .then((thing) => {
            // Initialize the property values
            allAvailableResources = {
                oilLevel: readFromSensor("oilLevel"),
                tyrePressure: readFromSensor("tyrePressure"),
            };
            maintenanceNeeded = false;
            thing.setPropertyReadHandler("allAvailableResources", async () => allAvailableResources);
            thing.setPropertyReadHandler("availableResourceLevel", async () => availableResourceLevel);
            thing.setPropertyReadHandler("maintenanceNeeded", async () => maintenanceNeeded);
            thing.setPropertyReadHandler("totalMileage", async () => totalMileage);
            thing.setPropertyReadHandler("nextServiceMileage", async () => nextServiceMileage);
            thing.setPropertyReadHandler("doorStatus", async () => doorStatus);
            // Override a write handler for nextServiceMileage property,
            // raising maintenanceNeeded flag when the interval exceeds 30,000 km
            thing.setPropertyWriteHandler("nextServiceMileage", async (val) => {
                nextServiceMileage = await val.value();
                // If counter for next service mileage is less than 500, set maintenance needed
                if (nextServiceMileage < 500) {
                    maintenanceNeeded = true;
                    // Notify a "maintainer" when the value has changed
                    // (the notify function here simply logs a message to the console)
                    notify(
                        "admin@leetfleet.com",
                        `maintenanceNeeded property has changed, new value is: ${maintenanceNeeded}`
                    );
                    if (maintenanceNeddedHistory != maintenanceNeeded) {
                        maintenanceNeddedHistory = maintenanceNeeded;
                        thing.emitPropertyChange("maintenanceNeeded");
                    }
                    thing.emitEvent("eventMaintenanceNeeded", `Maintenance needed! - next scheduled service is due.`);        
                }
            });
            // Now initialize properties
            nextServiceMileage = readMilometerServiceInterval();
            totalMileage = readMilometer();

            // Override a write handler for availableResourceLevel property,
            // utilizing the uriVariables properly
            thing.setPropertyWriteHandler("availableResourceLevel", async (val, options) => {
                // Check if uriVariables are provided
                if (options && typeof options === "object" && "uriVariables" in options) {
                    const uriVariables = options.uriVariables;
                    if ("id" in uriVariables) {
                        const id = uriVariables.id;
                        allAvailableResources[id] = await val.value();
                        return;
                    }
                }
                throw Error("Please specify id variable as uriVariables.");
            });
            // Override a read handler for availableResourceLevel property,
            // utilizing the uriVariables properly
            thing.setPropertyReadHandler("availableResourceLevel", async (options) => {
                // Check if uriVariables are provided
                if (options && typeof options === "object" && "uriVariables" in options) {
                    const uriVariables = options.uriVariables;
                    if ("id" in uriVariables) {
                        const id = uriVariables.id;
                        return allAvailableResources[id];
                    }
                }
                throw Error("Please specify id variable as uriVariables.");
            });
            // // Add write handler for maintenanceNedded property, enabling to reset it
            // thing.setPropertyWriteHandler("maintenanceNeeded", async (val) => {
            //     maintenanceNeeded = await val.value();
            // });
            // Set up a action handler for lockDoor
            thing.setActionHandler("lockDoor", async () => {
                doorStatus = "LOCKED";
                return doorStatus;
            });
            // Set up a action handler for unlockDoor
            thing.setActionHandler("unlockDoor", async () => {
                doorStatus = "UNLOCKED";
                return doorStatus;
            });

            // Emulation: increase milometer every second
            setInterval(() => {
                totalMileage = readMilometer();
                // If counter for next service mileage is less than 500, set maintenance needed
                if (nextServiceMileage < 500) {
                    if (!nextServiceIsDue) {
                        nextServiceIsDue = true;
                        maintenanceNeeded = true;
                        // Write log message to console only once
                        // Notify a "maintainer" when the value has changed
                        // (the notify function here simply logs a message to the console)
                        notify(
                                "admin@leetfleet.com",
                                `maintenanceNeeded property has changed, new value is: ${maintenanceNeeded}`
                            );    
                        if (maintenanceNeddedHistory != maintenanceNeeded) {
                            maintenanceNeddedHistory = maintenanceNeeded;
                            thing.emitPropertyChange("maintenanceNeeded");
                        }
                        thing.emitEvent("eventMaintenanceNeeded", `Maintenance needed! - next scheduled service is due.`);        
                    }
                }
            }, 1000);  

            // Emulation: decrease oil level every five seconds
            setInterval(() => {
                oilLevel = readFromSensor("oilLevel");
                // If oil level drops below 70%, then maintenance is needed
                if (oilLevel < 70) {
                    if (!oilLevelIsLow) {
                        oilLevelIsLow = true;
                        maintenanceNeeded = true;
                        // Write log message to console only once
                        // Notify a "maintainer" when the value has changed
                        // (the notify function here simply logs a message to the console)
                        notify(
                                "admin@leetfleet.com",
                                `maintenanceNeeded property has changed, new value is: ${maintenanceNeeded}`
                            );    
                            if (maintenanceNeddedHistory != maintenanceNeeded) {
                                maintenanceNeddedHistory = maintenanceNeeded;
                                thing.emitPropertyChange("maintenanceNeeded");
                            }
                            thing.emitEvent("eventMaintenanceNeeded", `Maintenance needed! - oil level is low.`);        
                    }
                }
            }, 5000);  

            // Emulation: decrease tyre pressure every ten seconds
            setInterval(() => {
                tyrePressure = readFromSensor("tyrePressure");
                // If oil level drops below 20 PSI, then maintenance is needed
                if (oilLevel < 20) {
                    if (!tyrePressureIsLow) {
                        tyrePressureIsLow = true;
                        maintenanceNeeded = true;
                        // Write log message to console only once
                        // Notify a "maintainer" when the value has changed
                        // (the notify function here simply logs a message to the console)
                        notify(
                                "admin@leetfleet.com",
                                `maintenanceNeeded property has changed, new value is: ${maintenanceNeeded}`
                            );    
                        if (maintenanceNeddedHistory != maintenanceNeeded) {
                            maintenanceNeddedHistory = maintenanceNeeded;
                            thing.emitPropertyChange("maintenanceNeeded");
                        }
                        thing.emitEvent("eventMaintenanceNeeded", `Maintenance needed! - tyre pressure is low.`);        
                    }
                }
            }, 10000);  

            // Finally expose the thing
            thing.expose().then(() => {
                console.info(`${thing.getThingDescription().title} ready`);
            });
            console.log(`Produced ${thing.getThingDescription().title}`);
        })
        .catch((e) => {
            console.log(e);
        });
    function readFromSensor(sensorType) {
        let sensorValue;
        if (sensorType === "tyrePressure") {
            // Decrease pressure between 1 and 3 PSI
            tyrePressure -= getRandomInt(0,3);
            sensorValue = tyrePressure;
            console.log("Reading sensor - tyrePressure: " + tyrePressure);
        } else if (sensorType === "oilLevel") {
            // Decrease oil level between 1 and 5%
            oilLevel -= getRandomInt(0,5);
            sensorValue = oilLevel;
            console.log("Reading sensor - oilLevel: " + oilLevel);
        }
        return sensorValue
    }
    function notify(subscribers, msg) {
        // Actual implementation of notifying subscribers with a message can go here
        console.log(msg);
    } 
    function readMilometerServiceInterval() {
        return nextServiceMileage;
    }   
    function readMilometer() {
        // Emulate mileage by increasing it randomly between 0 and 500 km
        mileageIncrease = getRandomInt(0, 500);
        totalMileage += mileageIncrease;
        nextServiceMileage -= mileageIncrease;
        console.log("Reading milometer: " + totalMileage);
        console.log("Distance left until next service is due: " + nextServiceMileage);
        return totalMileage;
    }
    function getRandomInt(min, max) {
        // round min value upwards to next integer value
        min = Math.ceil(min);
        // round max value downwards to next integer value
        max = Math.floor(max);
        // return a random value where max is inclusive and minimum is exclusive
        return Math.floor(Math.random() * (max - min) + min);
    }
});