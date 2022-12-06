/********************************************************************************
Smart-Vehicle with Node-WoT as dependency
********************************************************************************/
import * as WoT from "wot-typescript-definitions";

import request = require("request");

const Ajv = require("ajv");
const ajv = new Ajv(); // options can be passed, e.g. {allErrors: true}

export class WotDevice {
    public thing: WoT.ExposedThing;
    public deviceWoT: typeof WoT;
    public td: WoT.ExposedThingInit;
    public vehicleNumber = "1";

    // Status variables which are needed for emulation purposes
    private varTyrePressure = 35; // PSI
    private varOilLevel = 100; // Percent
    private varServiceDistance = 15000; // km
    private varTotalMileage = 44; // km
    private varTyrePressureIsLow = false;
    private varOilLevelIsLow = false;
    private varServiceIsDue = false;
    private varMaintenanceNeddedHistory: boolean;

    // ------------------------------------------------------------------------
    // Thing Model
    // ------------------------------------------------------------------------
    private thingModel: WoT.ExposedThingInit = {
        "@context": ["https://www.w3.org/2019/wot/td/v1", { "@language": "en" }],
        "@type": "",
        title: `smart-vehicle-${this.vehicleNumber}`,
        description: "Smart Vehicle",
        securityDefinitions: {
            "": {
                scheme: "nosec",
            },
        },
        security: "nosec_sc",
        properties: {
            propFleetId: {
                title: "prop-fleet-id",
                description: "Property fleet ID",
                unit: "",
                type: "string",
            },
            propVehicleId: {
                title: "prop-vehicle-id",
                description: "Property vehicle ID",
                unit: "",
                type: "string",
            },
            propOilLevel: {
                title: "prop-oil-level",
                description: "Property oil level",
                unit: "",
                type: "number",
                minimum: 0,
                maximum: 100,
                readOnly: true
            },
            propTyrePressure: {
                title: "prop-tyre-pressure",
                description: "Property tyre pressure",
                unit: "",
                type: "number",
                minimum: 0,
                maximum: 50,
                readOnly: true
            },
            propTotalMileage: {
                title: "prop-total-mileage",
                description: "Property total mileage",
                unit: "",
                type: "number",
                minimum: 0,
                readOnly: true,
                observable: true
            },
            propServiceDistance: {
                title: "prop-service-distance",
                description: "Property remaining distance until next service is due",
                unit: "",
                type: "number",
                maximum: 30000,
                observable: true
            },
            propDoorStatus: {
                title: "prop-door-status",
                description: "Property door status 'LOCKED' or 'UNLOCKED'",
                unit: "",
                type: "string",
                observable: true,
                readonly: true
            },
            propMaintenanceNeeded: {
                title: "prop-maintenance-needed",
                description: "Property maintenance needed",
                unit: "",
                type: "boolean",
                observable: true,
            },
        },
        actions: {
            actionLockDoor: {
                title: "action-lock-door",
                description: "Action lock the car door",
                input: {
                    unit: "",
                    type: "null",
                },
                out: {
                    unit: "",
                    type: "string",
                },
            },
            actionUnlockDoor: {
                title: "action-unlock-door",
                description: "Action unlock the car door",
                input: {
                    unit: "",
                    type: "null",
                },
                out: {
                    unit: "",
                    type: "string",
                },
            },
        },
        events: {
            eventLowOnOil: {
                title: "event-low-on-oil",
                description: "",
                data: {
                    unit: "",
                    type: "string",
                }
            },
            eventLowTyrePressure: {
                title: "event-low-tyre-pressure",
                description: "",
                data: {
                    unit: "",
                    type: "string",
                }
            },
            eventMaintenanceNeeded: {
                title: "event-maintenance-needed",
                description: "",
                data: {
                    unit: "",
                    type: "string",
                }
            },
        },
    };

    //TD Directory
    private tdDirectory: string;

    // Property declarations
    private propFleetId: WoT.InteractionInput;
    private propVehicleId: WoT.InteractionInput;
    private propOilLevel: WoT.InteractionInput;
    private propTyrePressure: WoT.InteractionInput;
    private propMaintenanceNeeded: WoT.InteractionInput;
    private propTotalMileage: WoT.InteractionInput;
    private propDoorStatus: WoT.InteractionInput;
    private propServiceDistance: WoT.InteractionInput;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    constructor(deviceWoT: typeof WoT, tdDirectory?: string, vehicleNumber?: string) {
        // initialze WotDevice parameters
        this.deviceWoT = deviceWoT;
        this.vehicleNumber = vehicleNumber;
        if (tdDirectory) this.tdDirectory = tdDirectory;
    }

    // ------------------------------------------------------------------------
    // Start Device - This method is invoked externally
    // ------------------------------------------------------------------------
    public async startDevice() {
        console.log(`Producing Thing: ${this.thingModel.title}`);
        const exposedThing = await this.deviceWoT.produce(this.thingModel);
        console.log("Thing produced");

        this.thing = exposedThing;
        this.td = exposedThing.getThingDescription();
        this.initialiseProperties(); // Initialize properties and add their handlers
        this.initialiseActions(); // Initialize actions and add their handlers
        // Events do not need to be initialzed, can be emited from anywhere

        console.log(`Exposing Thing: ${this.thingModel.title}`);
        await this.thing.expose(); // Expose thing
        console.log("Exposed Thing");

        if (this.tdDirectory) {
            this.register(this.tdDirectory);
        }
        this.listenToMyEvent(); // used to listen to specific events provided by a library. If you don't have events, simply remove it
    }

    // ------------------------------------------------------------------------
    // Register Thing Description with directory
    // ------------------------------------------------------------------------
    public register(directory: string) {
        console.log("Registering TD in directory: " + directory);
        // request.post(directory, { json: this.thing.getThingDescription() }, (error: any, response: { statusCode: number; }, body: any) => {
        request.put(directory + this.td.id, { json: this.thing.getThingDescription() }, (error: any, response: { statusCode: number; }, body: any) => {
                if (!error && response.statusCode < 300) {
                console.log("TD has been registered with the ID: " + this.td.id);
            } else {
                console.debug(error);
                console.debug(response);
                console.warn("Failed to register TD. Will try again in 10 Seconds...");
                setTimeout(() => {
                    this.register(directory);
                }, 15000);
                return;
            }
        });
    }

    // ------------------------------------------------------------------------
    // Action Handlers
    // ------------------------------------------------------------------------

    // Action handler for "lock door"
    private async lockDoorActionHandler(inputData?: WoT.InteractionOutput, options?: WoT.InteractionOptions) {
        // do something with inputData if available
        // let dataValue: string | number | boolean | object | WoT.DataSchemaValue[];
        // if (inputData) {
        //     dataValue = await inputData.value();
        // }
        // resolve that with outputData if available,
        // otherwise resolve action was successful without returning anything
        let outputData = "LOCKED";
        this.propDoorStatus = outputData;
        if (outputData.length != 0) {
            return outputData;
        } else {
            return null;
        }
    }

    // Action handler for "unlock door"
    private async unlockDoorActionHandler(inputData?: WoT.InteractionOutput, options?: WoT.InteractionOptions) {
        // do something with inputData if available
        // let dataValue: string | number | boolean | object | WoT.DataSchemaValue[];
        // if (inputData) {
        //     dataValue = await inputData.value();
        // }
        // resolve that with outputData if available,
        // otherwise resolve action was successful without returning anything
        let outputData = "UNLOCKED";
        this.propDoorStatus = outputData;
        if (outputData.length != 0) {
            return outputData;
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // Initialise properties
    // ------------------------------------------------------------------------
    private initialiseProperties() {
        // Property Fleet ID
        this.propFleetId = "not_defined";
        this.thing.setPropertyReadHandler("propFleetId", async () => this.propFleetId);
        this.thing.setPropertyWriteHandler("propFleetId", async (inputData: WoT.InteractionOutput, options?: WoT.InteractionOptions) => {
            let dataValue = await inputData.value();
            if (!ajv.validate(this.td.properties.propFleetId, dataValue)) {
                throw new Error("Writing the property 'propFleetId' failed due to a invalid input value.")
            } else {
                this.propFleetId = dataValue;
                this.thing.emitPropertyChange("propFleetId");
            }
        });

        // Property Vehicle ID
        this.propVehicleId = "XAKALAGLG1395940";
        this.thing.setPropertyReadHandler("propVehicleId", async () => this.propVehicleId);
        
        // Property Oil Level
        this.thing.setPropertyReadHandler("propOilLevel", async () => this.propOilLevel = this.varOilLevel);

        // Property Tyre Pressure
        this.thing.setPropertyReadHandler("propTyrePressure", async () => this.propTyrePressure = this.varTyrePressure);

        // Property Maintenance Needed
        this.propMaintenanceNeeded = false;
        this.thing.setPropertyReadHandler("propMaintenanceNeeded",  async () => this.propMaintenanceNeeded);        
        this.thing.setPropertyWriteHandler("propMaintenanceNeeded", async (inputData: WoT.InteractionOutput, options?: WoT.InteractionOptions) => {
            let dataValue = await inputData.value();
            if (!ajv.validate(this.td.properties.propMaintenanceNeeded, dataValue)) {
                throw new Error("Writing the property 'propMaintenanceNeeded' failed due to a invalid input value.")
            } else {
                this.propMaintenanceNeeded = dataValue;
                this.thing.emitPropertyChange("propMaintenanceNeeded");
            }
        });

        // Property Total Mileage
        this.thing.setPropertyReadHandler("propTotalMileage", async () => this.propTotalMileage = this.varTotalMileage);

        // Property Next-Service-Distance
        this.thing.setPropertyReadHandler("propServiceDistance", async () => this.propServiceDistance = this.varServiceDistance);
        this.thing.setPropertyWriteHandler("propServiceDistance", async (inputData: WoT.InteractionOutput, options?: WoT.InteractionOptions) => {
            let dataValue = await inputData.value();
            if (!ajv.validate(this.td.properties.propServiceDistance, dataValue)) {
                throw new Error("Writing the property 'propServiceDistance' failed due to a invalid input value.")
            } else {
                this.propServiceDistance = dataValue;
                this.thing.emitPropertyChange("propServiceDistance");
            }
        });

        // Property Door Status
        this.propDoorStatus = "UNLOCKED";
        this.thing.setPropertyReadHandler("propDoorStatus", async () => this.propDoorStatus);

    }

    // ------------------------------------------------------------------------
    // Initialise actions
    // ------------------------------------------------------------------------
    private initialiseActions() {
        // Set up a action handler for lockDoor
        this.thing.setActionHandler("actionLockDoor", async () => {
            return this.lockDoorActionHandler();
        });
        // Set up a action handler for unlockDoor
        this.thing.setActionHandler("actionUnlockDoor", async () => {
            return this.unlockDoorActionHandler();
        });

        // this.thing.setActionHandler("myAction", async (inputData) => {
        //     let dataValue = await inputData.value();
        //     if (!ajv.validate(this.td.actions.myAction.input, dataValue)) {
        //         throw new Error("Invalid input");
        //     } else {
        //         return this.myActionHandler(inputData);
        //     }
        // });
    }

    // ------------------------------------------------------------------------
    // Optional: Event listener for incoming events
    // ------------------------------------------------------------------------
    private listenToMyEvent() {
        /*
        specialLibrary.getMyEvent()//change specialLibrary to your library
        .then((thisEvent) => {
            this.thing.emitEvent("myEvent",""); //change quotes to your own event data
        });
        */
    }

    // ------------------------------------------------------------------------
    // Emulation helper functions
    // ------------------------------------------------------------------------

    private emulateAndReadSensor(sensorType: string) {
        let sensorValue: number;
        if (sensorType === "tyrePressure") {
            // Decrease pressure between 1 and 3 PSI
            this.varTyrePressure -= this.getRandomInt(0, 3);
            sensorValue = this.varTyrePressure;
            console.log("Reading sensor - tyrePressure: " + this.varTyrePressure);
        } else if (sensorType === "oilLevel") {
            // Decrease oil level between 1 and 5%
            this.varOilLevel -= this.getRandomInt(0, 5);
            sensorValue = this.varOilLevel;
            console.log("Reading sensor - oilLevel: " + this.varOilLevel);
        }
        return sensorValue
    }

    private notify(subscribers, msg) {
        // Actual implementation of notifying subscribers with a message can go here
        console.log(msg);
    }

    private emulateOdometer() {
        // Emulate mileage by increasing it randomly between 100 and 999 km
        let mileageIncrease: number;
        mileageIncrease = this.getRandomInt(100, 999);
        this.varTotalMileage += mileageIncrease;
        this.varServiceDistance -= mileageIncrease;
        console.log("Reading milometer: " + this.varTotalMileage);
        console.log("Distance left until next service is due: " + this.varServiceDistance);
        // return this.varTotalMileage;
        // return {'totalMileage': this.varTotalMileage, 'nextServiceDistance': this.varNextServiceDistance};
    }

    private getRandomInt(min: number, max: number) {
        // round min value upwards to next integer value
        min = Math.ceil(min);
        // round max value downwards to next integer value
        max = Math.floor(max);
        // return a random value where max is inclusive and minimum is exclusive
        return Math.floor(Math.random() * (max - min) + min);
    }

    // ------------------------------------------------------------------------
    // Emulation - This method is invoked externally
    // ------------------------------------------------------------------------
    public async emulateDevice() {

        // Delay the emulation for two second to allow completing the registration
        // proccess (TD) with the WoTHive directory before starting to emulate the device.
        await new Promise(resolve => setTimeout(resolve, 2000));

        // Emulation: decrease oil level every five seconds
        setInterval(() => {
            this.propOilLevel = this.emulateAndReadSensor("oilLevel");
            // If oil level drops below 70%, then maintenance is needed
            if (this.propOilLevel < 70) {
                if (!this.varOilLevelIsLow) {
                    this.varOilLevelIsLow = true
                    this.propMaintenanceNeeded = true;
                    // Write log message to console only once
                    // Notify a "maintainer" when the value has changed
                    // (the notify function here simply logs a message to the console)
                    this.notify(
                            "admin@leetfleet.com",
                            `propMaintenanceNeeded property has changed, new value is: ${this.propMaintenanceNeeded}`
                        );    
                        if (this.varMaintenanceNeddedHistory != this.propMaintenanceNeeded) {
                            this.varMaintenanceNeddedHistory  = this.propMaintenanceNeeded;
                            this.thing.emitPropertyChange("propMaintenanceNeeded");
                        }
                        this.thing.emitEvent("eventMaintenanceNeeded", `Maintenance needed! - oil level is low.`);        
                }
            }
        }, 15000);

        // Emulation: decrease tyre pressure every ten seconds
        setInterval(() => {
            this.propTyrePressure = this.emulateAndReadSensor("tyrePressure");
            // If oil level drops below 20 PSI, then maintenance is needed
            if (this.propTyrePressure < 20) {
                if (!this.varTyrePressureIsLow) {
                    this.varTyrePressureIsLow = true;
                    this.propMaintenanceNeeded = true;
                    // Write log message to console only once
                    // Notify a "maintainer" when the value has changed
                    // (the notify function here simply logs a message to the console)
                    this.notify(
                            "admin@leetfleet.com",
                            `propMaintenanceNeeded property has changed, new value is: ${this.propMaintenanceNeeded}`
                        );    
                        if (this.varMaintenanceNeddedHistory != this.propMaintenanceNeeded) {
                            this.varMaintenanceNeddedHistory  = this.propMaintenanceNeeded;
                            this.thing.emitPropertyChange("propMaintenanceNeeded");
                        }
                        this.thing.emitEvent("eventMaintenanceNeeded", `Maintenance needed! - tyre pressure is low.`);        
                }
            }
        }, 15000);

        // Emulation: increase milometer every second
        setInterval(() => {
            this.emulateOdometer();
            this.thing.emitPropertyChange("propTotalMileage");
            this.thing.emitPropertyChange("propServiceDistance");        
            // If counter for next service mileage is less than 500, set maintenance needed
            if (this.varServiceDistance < 500) {
                if (!this.varServiceIsDue) {
                    this.varServiceIsDue = true;
                    this.propMaintenanceNeeded = true;
                    // Write log message to console only once
                    // Notify a "maintainer" when the value has changed
                    // (the notify function here simply logs a message to the console)
                    this.notify(
                            "admin@leetfleet.com",
                            `propMaintenanceNeeded property has changed, new value is: ${this.propMaintenanceNeeded}`
                        );    
                        if (this.varMaintenanceNeddedHistory != this.propMaintenanceNeeded) {
                            this.varMaintenanceNeddedHistory  = this.propMaintenanceNeeded;
                            this.thing.emitPropertyChange("propMaintenanceNeeded");
                        }
                    this.thing.emitEvent("eventMaintenanceNeeded", `Maintenance needed! - next scheduled service is due.`);        
                }
            }
        }, 10000);
    }
}