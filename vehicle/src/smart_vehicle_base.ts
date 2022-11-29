/********************************************************************************
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the W3C Software Notice and
 * Document License (2015-05-13) which is available at
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document.
 *
 * SPDX-License-Identifier: EPL-2.0 OR W3C-20150513
 ********************************************************************************/
import * as WoT from "wot-typescript-definitions";

import request = require("request");

//  import Ajv = require("ajv");
//  var ajv = new Ajv();

const Ajv = require("ajv");
const ajv = new Ajv(); // options can be passed, e.g. {allErrors: true}

export class WotDevice {
    public thing: WoT.ExposedThing;
    public deviceWoT: typeof WoT;
    public td: WoT.ExposedThingInit;

    // Definition of member variables
    varTyrePressure = 35;
    varOilLevel = 100;
    varNextServiceDistance = 30000;
    varTotalMileage = 0;
    // Status variables - used for emulation purposes
    varTyrePressureIsLow = false;
    varOilLevelIsLow = false;
    varNextServiceIsDue = false;
    varMaintenanceNedded = false;
    varMaintenanceNeddedHistory = false;

    // ------------------------------------------------------------------------
    // Thing Model
    // ------------------------------------------------------------------------
    private thingModel: WoT.ExposedThingInit = {
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

    //TD Directory
    private tdDirectory: string;

    // property declarations
    private propOilLevel: WoT.InteractionInput;
    private propTyrePressure: WoT.InteractionInput;
    private propMaintenanceNeeded: WoT.InteractionInput;
    private propTotalMileage: WoT.InteractionInput;
    private propNextServiceDistance: WoT.InteractionInput;
    private propDoorStatus: WoT.InteractionInput;
    private propFleetId: WoT.InteractionInput;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    constructor(deviceWoT: typeof WoT, tdDirectory?: string) {
        // initialze WotDevice parameters
        this.deviceWoT = deviceWoT;
        if (tdDirectory) this.tdDirectory = tdDirectory;
    }

    // ------------------------------------------------------------------------
    // Produce the Thing, expose it, and intialise properties and actions
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
        this.emulateBehaviour();

        console.log(`Exposing Thing: ${this.thingModel.title}`);
        await this.thing.expose(); // Expose thing
        console.log("Exposed Thing");

        if (this.tdDirectory) {
            this.register(this.tdDirectory);
        }
        
        
    }

    // ------------------------------------------------------------------------
    // Register Thing Description with directory
    // ------------------------------------------------------------------------
    public register(directory: string) {
        console.log("Registering TD in directory: " + directory);
        request.post(directory, { json: this.thing.getThingDescription() }, (error: any, response: { statusCode: number; }, body: any) => {
            if (!error && response.statusCode < 300) {
                console.log("TD registered!");
            } else {
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
    private async propFleetIdReadHandler(options?: WoT.InteractionOptions) {
        return this.propFleetId;
    }

    private async propFleetIdWriteHandler(inputData: WoT.InteractionOutput, options?: WoT.InteractionOptions) {        
        this.propFleetId = await inputData.value();
        this.thing.emitPropertyChange("propFleetId");
    }

    // Oil Level
    private async propOilLevelReadHandler(options?: WoT.InteractionOptions) {
        this.propOilLevel = this.readFromSensor("oilLevel");
        return this.propOilLevel;
    }

    // Tyre Pressure
    private async propTyrePressureReadHandler(options?: WoT.InteractionOptions) {
        this.propOilLevel = this.readFromSensor("tyrePressure");
        return this.propTyrePressure;
    }

    // Maintenance Needed
    private async propMaintenanceNeededReadHandler(options?: WoT.InteractionOptions) {
        this.propMaintenanceNeeded = this.isMaintenanceNeeded();
        return this.propMaintenanceNeeded;
    }

    private async propMaintenanceNeededWriteHandler(inputData: WoT.InteractionOutput, options?: WoT.InteractionOptions) {        
        this.propMaintenanceNeeded = await inputData.value();
        this.thing.emitPropertyChange("propMaintenanceNeeded");
    }

    // Total Mileage
    private async propTotalMileageReadHandler(options?: WoT.InteractionOptions) {
        this.propTotalMileage = this.readOdometer();
        return this.propTotalMileage;
    }

    // Next-Service-Distance
    private async propNextServiceDistanceReadHandler(options?: WoT.InteractionOptions) {
        this.propNextServiceDistance = this.readOdometerServiceInterval();
        return this.propNextServiceDistance;
    }

    private async propNextServiceDistanceWriteHandler(inputData: WoT.InteractionOutput, options?: WoT.InteractionOptions) {        
        this.propNextServiceDistance = await inputData.value();
        this.thing.emitPropertyChange("propNextServiceDistance");
    }

    // Door Status
    private async propDoorStatusReadHandler(options?: WoT.InteractionOptions) {
        return this.propDoorStatus;
    }

    // ------------------------------------------------------------------------
    // Action Handlers
    // ------------------------------------------------------------------------

    // Action handler for "lock door"
    private async lockDoorActionHandler(inputData?: WoT.InteractionOutput, options?: WoT.InteractionOptions) {
        // do something with inputData if available
        let dataValue: string | number | boolean | object | WoT.DataSchemaValue[];
        if (inputData) {
            dataValue = await inputData.value();
        }
        // resolve that with outputData if available,
        // otherwise resolve action was successful without returning anything
        let outputData = "LOCKED";
        if (outputData.length != 0) {
            return outputData;
        } else {
            return null;
        }
    }

    // Action handler for "unlock door"
    private async unlockDoorActionHandler(inputData?: WoT.InteractionOutput, options?: WoT.InteractionOptions) {
        // do something with inputData if available
        let dataValue: string | number | boolean | object | WoT.DataSchemaValue[];
        if (inputData) {
            dataValue = await inputData.value();
        }
        // resolve that with outputData if available,
        // otherwise resolve action was successful without returning anything
        let outputData = "UNLOCKED";
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
        this.propFleetId = "unknown";
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

    private readFromSensor(sensorType) {
        let sensorValue;
        if (sensorType === "tyrePressure") {
            // Decrease pressure between 1 and 3 PSI
            this.varTyrePressure -= this.getRandomInt(0,3);
            sensorValue = this.varTyrePressure;
            console.log("Reading sensor - tyrePressure: " + this.varTyrePressure);
        } else if (sensorType === "oilLevel") {
            // Decrease oil level between 1 and 5%
            this.varOilLevel -= this.getRandomInt(0,5);
            sensorValue = this.varOilLevel;
            console.log("Reading sensor - oilLevel: " + this.varOilLevel);
        }
        return sensorValue
    }

    private notify(subscribers, msg) {
        // Actual implementation of notifying subscribers with a message can go here
        console.log(msg);
    } 

    private readOdometerServiceInterval() {
        return this.varNextServiceDistance;
    } 

    private readOdometer() {
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
    
    private getRandomInt(min, max) {
        // round min value upwards to next integer value
        min = Math.ceil(min);
        // round max value downwards to next integer value
        max = Math.floor(max);
        // return a random value where max is inclusive and minimum is exclusive
        return Math.floor(Math.random() * (max - min) + min);
    }

    private isMaintenanceNeeded() {
        if (this.varNextServiceDistance < 500) {
            this.varMaintenanceNedded = true;
            // Notify a "maintainer" when the value has changed
            // (the notify function here simply logs a message to the console)
            this.notify(
                "admin@leetfleet.com",
                `maintenanceNeeded property has changed, new value is: ${this.varMaintenanceNeddedHistory}`
            );
            this.thing.emitEvent("eventMaintenanceNeeded", `Maintenance needed! - next scheduled service is due.`);        
        } else {
            this.varMaintenanceNedded = false;
        }
        if (this.varMaintenanceNeddedHistory != this.varMaintenanceNedded) {
            this.varMaintenanceNeddedHistory = this.varMaintenanceNedded;
            this.thing.emitPropertyChange("maintenanceNeeded");
        }
        return this.varMaintenanceNedded
    }

    private emulateBehaviour() {
        ;
    }
}