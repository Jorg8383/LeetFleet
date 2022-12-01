"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.WotDevice = void 0;
const request = require("request");
const Ajv = require("ajv");
const ajv = new Ajv(); // options can be passed, e.g. {allErrors: true}
class WotDevice {
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    constructor(deviceWoT, tdDirectory) {
        // Status variables - used for emulation purposes
        // private varTyrePressure = 35;
        // private varOilLevel = 100;
        // private varNextServiceDistance = 30000;
        // private varTotalMileage = 0;
        // private varTyrePressureIsLow = false;
        // private varOilLevelIsLow = false;
        // private varNextServiceIsDue = false;
        // private varMaintenanceNedded = false;
        // private varMaintenanceNeddedHistory = false;
        // ------------------------------------------------------------------------
        // Thing Model
        // ------------------------------------------------------------------------
        this.thingModel = {
            "@context": ["https://www.w3.org/2019/wot/td/v1", { "@language": "en" }],
            "@type": "",
            id: "urn:dev:ops:smart-vehicle-1",
            title: "smart-vehicle",
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
                    readOnly: true
                },
                propServiceDistance: {
                    title: "prop-service-distance",
                    description: "Property remaining distance until next service is due",
                    unit: "",
                    type: "number",
                    maximum: 30000
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
        // initialze WotDevice parameters
        this.deviceWoT = deviceWoT;
        if (tdDirectory)
            this.tdDirectory = tdDirectory;
    }
    // ------------------------------------------------------------------------
    // Produce the Thing, expose it, and intialise properties and actions
    // ------------------------------------------------------------------------
    startDevice() {
        return __awaiter(this, void 0, void 0, function* () {
            console.log(`Producing Thing: ${this.thingModel.title}`);
            const exposedThing = yield this.deviceWoT.produce(this.thingModel);
            console.log("Thing produced");
            this.thing = exposedThing;
            this.td = exposedThing.getThingDescription();
            this.initialiseProperties(); // Initialize properties and add their handlers
            this.initialiseActions(); // Initialize actions and add their handlers
            // Events do not need to be initialzed, can be emited from anywhere
            console.log(`Exposing Thing: ${this.thingModel.title}`);
            yield this.thing.expose(); // Expose thing
            console.log("Exposed Thing");
            if (this.tdDirectory) {
                this.register(this.tdDirectory);
            }
            this.listenToMyEvent(); // used to listen to specific events provided by a library. If you don't have events, simply remove it
        });
    }
    // ------------------------------------------------------------------------
    // Register Thing Description with directory
    // ------------------------------------------------------------------------
    register(directory) {
        console.log("Registering TD in directory: " + directory);
        request.post(directory, { json: this.thing.getThingDescription() }, (error, response, body) => {
            if (!error && response.statusCode < 300) {
                console.log("TD registered!");
            }
            else {
                console.debug(error);
                console.debug(response);
                console.warn("Failed to register TD. Will try again in 10 Seconds...");
                setTimeout(() => {
                    this.register(directory);
                }, 10000);
                return;
            }
        });
    }
    // ------------------------------------------------------------------------
    // Action Handlers
    // ------------------------------------------------------------------------
    // Action handler for "lock door"
    lockDoorActionHandler(inputData, options) {
        return __awaiter(this, void 0, void 0, function* () {
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
            }
            else {
                return null;
            }
        });
    }
    // Action handler for "unlock door"
    unlockDoorActionHandler(inputData, options) {
        return __awaiter(this, void 0, void 0, function* () {
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
            }
            else {
                return null;
            }
        });
    }
    // ------------------------------------------------------------------------
    // Initialise properties
    // ------------------------------------------------------------------------
    initialiseProperties() {
        // Property Fleet ID
        this.propFleetId = "not_defined";
        this.thing.setPropertyReadHandler("propFleetId", () => __awaiter(this, void 0, void 0, function* () { return this.propFleetId; }));
        this.thing.setPropertyWriteHandler("propFleetId", (inputData, options) => __awaiter(this, void 0, void 0, function* () {
            let dataValue = yield inputData.value();
            if (!ajv.validate(this.td.properties.propFleetId, dataValue)) {
                throw new Error("Writing the property 'propFleetId' failed due to a invalid input value.");
            }
            else {
                this.propFleetId = dataValue;
                this.thing.emitPropertyChange("propFleetId");
            }
        }));
        // Property Oil Level
        this.propOilLevel = 100; // [%]; // replace quotes with the initial value
        this.thing.setPropertyReadHandler("propOilLevel", () => __awaiter(this, void 0, void 0, function* () { return this.propOilLevel; }));
        // Property Tyre Pressure
        this.propTyrePressure = 35; // [PSI]
        this.thing.setPropertyReadHandler("propTyrePressure", () => __awaiter(this, void 0, void 0, function* () { return this.propTyrePressure; }));
        // Property Maintenance Needed
        this.propMaintenanceNeeded = false;
        this.thing.setPropertyReadHandler("propMaintenanceNeeded", () => __awaiter(this, void 0, void 0, function* () { return this.propMaintenanceNeeded; }));
        this.thing.setPropertyWriteHandler("propMaintenanceNeeded", (inputData, options) => __awaiter(this, void 0, void 0, function* () {
            let dataValue = yield inputData.value();
            if (!ajv.validate(this.td.properties.propMaintenanceNeeded, dataValue)) {
                throw new Error("Writing the property 'propMaintenanceNeeded' failed due to a invalid input value.");
            }
            else {
                this.propMaintenanceNeeded = dataValue;
                this.thing.emitPropertyChange("propMaintenanceNeeded");
            }
        }));
        // Property Total Mileage
        this.propTotalMileage = 33;
        this.thing.setPropertyReadHandler("propTotalMileage", () => __awaiter(this, void 0, void 0, function* () { return this.propTotalMileage; }));
        // Property Next-Service-Distance
        this.propServiceDistance = 30000;
        this.thing.setPropertyReadHandler("propServiceDistance", () => __awaiter(this, void 0, void 0, function* () { return this.propServiceDistance; }));
        this.thing.setPropertyWriteHandler("propServiceDistance", (inputData, options) => __awaiter(this, void 0, void 0, function* () {
            let dataValue = yield inputData.value();
            if (!ajv.validate(this.td.properties.propServiceDistance, dataValue)) {
                throw new Error("Writing the property 'propServiceDistance' failed due to a invalid input value.");
            }
            else {
                this.propServiceDistance = dataValue;
                this.thing.emitPropertyChange("propServiceDistance");
            }
        }));
        // Property Door Status
        this.propDoorStatus = "UNLOCKED";
        this.thing.setPropertyReadHandler("propDoorStatus", () => __awaiter(this, void 0, void 0, function* () { return this.propDoorStatus; }));
    }
    // ------------------------------------------------------------------------
    // Initialise actions
    // ------------------------------------------------------------------------
    initialiseActions() {
        // Set up a action handler for lockDoor
        this.thing.setActionHandler("actionLockDoor", () => __awaiter(this, void 0, void 0, function* () {
            return this.lockDoorActionHandler();
        }));
        // Set up a action handler for unlockDoor
        this.thing.setActionHandler("actionUnlockDoor", () => __awaiter(this, void 0, void 0, function* () {
            return this.unlockDoorActionHandler();
        }));
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
    listenToMyEvent() {
        /*
        specialLibrary.getMyEvent()//change specialLibrary to your library
        .then((thisEvent) => {
            this.thing.emitEvent("myEvent",""); //change quotes to your own event data
        });
        */
    }
    // ------------------------------------------------------------------------
    // Emulation
    // ------------------------------------------------------------------------
    // private readFromSensor(sensorType) {
    //     let sensorValue: number;
    //     if (sensorType === "tyrePressure") {
    //         // Decrease pressure between 1 and 3 PSI
    //         this.varTyrePressure -= this.getRandomInt(0, 3);
    //         sensorValue = this.varTyrePressure;
    //         console.log("Reading sensor - tyrePressure: " + this.varTyrePressure);
    //     } else if (sensorType === "oilLevel") {
    //         // Decrease oil level between 1 and 5%
    //         this.varOilLevel -= this.getRandomInt(0, 5);
    //         sensorValue = this.varOilLevel;
    //         console.log("Reading sensor - oilLevel: " + this.varOilLevel);
    //     }
    //     return sensorValue
    // }
    // private notify(subscribers, msg) {
    //     // Actual implementation of notifying subscribers with a message can go here
    //     console.log(msg);
    // }
    // private readOdometerServiceInterval() {
    //     return this.varNextServiceDistance;
    // }
    // private readOdometer() {
    //     // Emulate mileage by increasing it randomly between 0 and 500 km
    //     let mileageIncrease;
    //     mileageIncrease = this.getRandomInt(0, 500);
    //     this.varTotalMileage += mileageIncrease;
    //     this.varNextServiceDistance -= mileageIncrease;
    //     console.log("Reading milometer: " + this.varTotalMileage);
    //     console.log("Distance left until next service is due: " + this.varNextServiceDistance);
    //     return this.varTotalMileage;
    //     // return {'totalMileage': totalMileage, 'nextServiceDistance': nextServiceDistance};
    // }
    // private getRandomInt(min, max) {
    //     // round min value upwards to next integer value
    //     min = Math.ceil(min);
    //     // round max value downwards to next integer value
    //     max = Math.floor(max);
    //     // return a random value where max is inclusive and minimum is exclusive
    //     return Math.floor(Math.random() * (max - min) + min);
    // }
    // private isMaintenanceNeeded() {
    //     if (this.varNextServiceDistance < 500) {
    //         this.varMaintenanceNedded = true;
    //         // Notify a "maintainer" when the value has changed
    //         // (the notify function here simply logs a message to the console)
    //         this.notify(
    //             "admin@leetfleet.com",
    //             `maintenanceNeeded property has changed, new value is: ${this.varMaintenanceNeddedHistory}`
    //         );
    //         this.thing.emitEvent("eventMaintenanceNeeded", `Maintenance needed! - next scheduled service is due.`);
    //     } else {
    //         this.varMaintenanceNedded = false;
    //     }
    //     if (this.varMaintenanceNeddedHistory != this.varMaintenanceNedded) {
    //         this.varMaintenanceNeddedHistory = this.varMaintenanceNedded;
    //         this.thing.emitPropertyChange("maintenanceNeeded");
    //     }
    //     return this.varMaintenanceNedded
    // }
    emulate() {
        return __awaiter(this, void 0, void 0, function* () {
            ;
        });
    }
}
exports.WotDevice = WotDevice;
//# sourceMappingURL=smart_vehicle_base.js.map