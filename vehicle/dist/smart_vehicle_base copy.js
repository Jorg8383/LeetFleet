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
        // Definition of member variables
        this.varTyrePressure = 35;
        this.varOilLevel = 100;
        this.varNextServiceDistance = 30000;
        this.varTotalMileage = 0;
        // Status variables - used for emulation purposes
        this.varTyrePressureIsLow = false;
        this.varOilLevelIsLow = false;
        this.varNextServiceIsDue = false;
        this.varMaintenanceNedded = false;
        this.varMaintenanceNeddedHistory = false;
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
                    type: "string",
                },
                propOilLevel: {
                    type: "integer",
                    minimum: 0,
                    maximum: 100,
                    readOnly: true
                },
                propTyrePressure: {
                    type: "integer",
                    minimum: 0,
                    maximum: 50,
                    readOnly: true
                },
                propTotalMileage: {
                    type: "integer",
                    description: `The total mileage of the vehicle.`,
                    minimum: 0,
                    readOnly: true
                },
                propNextServiceDistance: {
                    type: "integer",
                    description: `Mileage counter for service intervals.`,
                    minimum: -1000000,
                    maximum: 30000,
                },
                propDoorStatus: {
                    title: "Door status",
                    type: "string",
                    enum: ["LOCKED", "UNLOCKED"],
                    observable: true,
                    readonly: true
                },
                propMaintenanceNeeded: {
                    type: "boolean",
                    description: `Indicates when maintenance is needed. The property is observable. Automatically set to true if oil or tyre pressure is too low.`,
                    observable: true,
                },
            },
            actions: {
                actionLockDoor: {
                    description: `Lock the car door`,
                    output: { type: "string" },
                },
                actionUnlockDoor: {
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
            this.emulateBehaviour();
            console.log(`Exposing Thing: ${this.thingModel.title}`);
            yield this.thing.expose(); // Expose thing
            console.log("Exposed Thing");
            if (this.tdDirectory) {
                this.register(this.tdDirectory);
            }
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
    // Property Handlers
    // ------------------------------------------------------------------------
    // Fleet ID
    propFleetIdReadHandler(options) {
        return __awaiter(this, void 0, void 0, function* () {
            console.log("Reading 'propFleetId'...." + this.propFleetId);
            return this.propFleetId;
        });
    }
    propFleetIdWriteHandler(inputData, options) {
        return __awaiter(this, void 0, void 0, function* () {
            let dataValue = yield inputData.value();
            if (!ajv.validate(this.td.properties.propFleetId, dataValue)) {
                throw new Error("Writing the property 'propFleetId' failed due to a invalid input value.");
            }
            else {
                this.propFleetId = dataValue;
                this.thing.emitPropertyChange("propFleetId");
            }
        });
    }
    // Oil Level
    propOilLevelReadHandler(options) {
        return __awaiter(this, void 0, void 0, function* () {
            this.propOilLevel = this.readFromSensor("oilLevel");
            return this.propOilLevel;
        });
    }
    // Tyre Pressure
    propTyrePressureReadHandler(options) {
        return __awaiter(this, void 0, void 0, function* () {
            this.propTyrePressure = this.readFromSensor("tyrePressure");
            return this.propTyrePressure;
        });
    }
    // Maintenance Needed
    propMaintenanceNeededReadHandler(options) {
        return __awaiter(this, void 0, void 0, function* () {
            this.propMaintenanceNeeded = this.isMaintenanceNeeded();
            return this.propMaintenanceNeeded;
        });
    }
    propMaintenanceNeededWriteHandler(inputData, options) {
        return __awaiter(this, void 0, void 0, function* () {
            let dataValue = yield inputData.value();
            if (!ajv.validate(this.td.properties.propMaintenanceNeeded, dataValue)) {
                throw new Error("Writing the property 'propMaintenanceNeeded' failed due to a invalid input value.");
            }
            else {
                this.propMaintenanceNeeded = dataValue;
                this.thing.emitPropertyChange("propMaintenanceNeeded");
            }
        });
    }
    // Total Mileage
    propTotalMileageReadHandler(options) {
        return __awaiter(this, void 0, void 0, function* () {
            this.propTotalMileage = this.readOdometer();
            return this.propTotalMileage;
        });
    }
    // Next-Service-Distance
    propNextServiceDistanceReadHandler(options) {
        return __awaiter(this, void 0, void 0, function* () {
            this.propNextServiceDistance = this.readOdometerServiceInterval();
            return this.propNextServiceDistance;
        });
    }
    propNextServiceDistanceWriteHandler(inputData, options) {
        return __awaiter(this, void 0, void 0, function* () {
            let dataValue = yield inputData.value();
            if (!ajv.validate(this.td.properties.propNextServiceDistance, dataValue)) {
                throw new Error("Writing the property 'propNextServiceDistance' failed due to a invalid input value.");
            }
            else {
                this.propNextServiceDistance = dataValue;
                this.thing.emitPropertyChange("propNextServiceDistance");
            }
        });
    }
    // Door Status
    propDoorStatusReadHandler(options) {
        return __awaiter(this, void 0, void 0, function* () {
            return this.propDoorStatus;
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
        this.propFleetId = "unknown";
        console.log("propFleetId:" + this.propFleetId);
        this.thing.setPropertyReadHandler("propFleetId", this.propFleetIdReadHandler);
        this.thing.setPropertyWriteHandler("propFleetId", this.propFleetIdWriteHandler);
        // Property Oil Level
        this.propOilLevel = 100; // [%]; // replace quotes with the initial value
        this.thing.setPropertyReadHandler("propOilLevel", this.propOilLevelReadHandler);
        // Property Tyre Pressure
        this.propTyrePressure = 35; // [PSI]
        this.thing.setPropertyReadHandler("propTyrePressure", this.propTyrePressureReadHandler);
        // Property Maintenance Needed
        this.propMaintenanceNeeded = false;
        this.thing.setPropertyReadHandler("propMaintenanceNeeded", this.propMaintenanceNeededReadHandler);
        this.thing.setPropertyWriteHandler("propMaintenanceNeeded", this.propMaintenanceNeededWriteHandler);
        // Property Total Mileage
        this.propTotalMileage = 0;
        this.thing.setPropertyReadHandler("propTotalMileage", this.propTotalMileageReadHandler);
        // Property Next-Service-Distance
        this.propNextServiceDistance = 30000;
        this.thing.setPropertyReadHandler("propNextServiceDistance", this.propNextServiceDistanceReadHandler);
        this.thing.setPropertyWriteHandler("propNextServiceDistance", this.propNextServiceDistanceWriteHandler);
        // Property Door Status
        this.propDoorStatus = "UNLOCKED";
        this.thing.setPropertyReadHandler("propDoorStatus", this.propDoorStatusReadHandler);
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
    readFromSensor(sensorType) {
        let sensorValue;
        if (sensorType === "tyrePressure") {
            // Decrease pressure between 1 and 3 PSI
            this.varTyrePressure -= this.getRandomInt(0, 3);
            sensorValue = this.varTyrePressure;
            console.log("Reading sensor - tyrePressure: " + this.varTyrePressure);
        }
        else if (sensorType === "oilLevel") {
            // Decrease oil level between 1 and 5%
            this.varOilLevel -= this.getRandomInt(0, 5);
            sensorValue = this.varOilLevel;
            console.log("Reading sensor - oilLevel: " + this.varOilLevel);
        }
        return sensorValue;
    }
    notify(subscribers, msg) {
        // Actual implementation of notifying subscribers with a message can go here
        console.log(msg);
    }
    readOdometerServiceInterval() {
        return this.varNextServiceDistance;
    }
    readOdometer() {
        // Emulate mileage by increasing it randomly between 0 and 500 km
        let mileageIncrease;
        mileageIncrease = this.getRandomInt(0, 500);
        this.varTotalMileage += mileageIncrease;
        this.varNextServiceDistance -= mileageIncrease;
        console.log("Reading milometer: " + this.varTotalMileage);
        console.log("Distance left until next service is due: " + this.varNextServiceDistance);
        return this.varTotalMileage;
        // return {'totalMileage': totalMileage, 'nextServiceDistance': nextServiceDistance};
    }
    getRandomInt(min, max) {
        // round min value upwards to next integer value
        min = Math.ceil(min);
        // round max value downwards to next integer value
        max = Math.floor(max);
        // return a random value where max is inclusive and minimum is exclusive
        return Math.floor(Math.random() * (max - min) + min);
    }
    isMaintenanceNeeded() {
        if (this.varNextServiceDistance < 500) {
            this.varMaintenanceNedded = true;
            // Notify a "maintainer" when the value has changed
            // (the notify function here simply logs a message to the console)
            this.notify("admin@leetfleet.com", `maintenanceNeeded property has changed, new value is: ${this.varMaintenanceNeddedHistory}`);
            this.thing.emitEvent("eventMaintenanceNeeded", `Maintenance needed! - next scheduled service is due.`);
        }
        else {
            this.varMaintenanceNedded = false;
        }
        if (this.varMaintenanceNeddedHistory != this.varMaintenanceNedded) {
            this.varMaintenanceNeddedHistory = this.varMaintenanceNedded;
            this.thing.emitPropertyChange("maintenanceNeeded");
        }
        return this.varMaintenanceNedded;
    }
    emulateBehaviour() {
        ;
    }
}
exports.WotDevice = WotDevice;
//# sourceMappingURL=smart_vehicle_base%20copy.js.map