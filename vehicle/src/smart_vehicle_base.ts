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

    // Thing Model -> fill in the empty quotation marks
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
            oilLevel: {
                type: "integer",
                minimum: 0,
                maximum: 100,
                observable: true,
                readOnly: true
            },
            tyrePressure: {
                type: "integer",
                minimum: 0,
                maximum: 50,
                observable: true,
                readOnly: true
            },
            totalMileage: {
                type: "integer",
                description: `The total mileage of the vehicle.`,
                minimum: 0,
                observable: true,
                readOnly: true
            },
            nextServiceDistance: {
                type: "integer",
                description: `Mileage counter for service intervals.`,
                minimum: -1000000,
                maximum: 30000,
                observable: true,
            },
            doorStatus: {
                title: "Door status",
                type: "string",
                enum: ["LOCKED", "UNLOCKED"],
                observable: true,
                readonly: true
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
    };

    //TD Directory
    private tdDirectory: string;

    // property declarations
    private myProperty: WoT.InteractionInput;
    private propOilLevel: WoT.InteractionInput;
    private propTyrePressure: WoT.InteractionInput;
    private propMaintenanceNeeded: WoT.InteractionInput;
    private propTotalMileage: WoT.InteractionInput;
    private propNextServiceDistance: WoT.InteractionInput;
    private propDoorStatus: WoT.InteractionInput;
    private propFleetId: WoT.InteractionInput;

    constructor(deviceWoT: typeof WoT, tdDirectory?: string) {
        // initialze WotDevice parameters
        this.deviceWoT = deviceWoT;
        if (tdDirectory) this.tdDirectory = tdDirectory;
    }

    public async startDevice() {
        console.log(`Producing Thing: ${this.thingModel.title}`);
        const exposedThing = await this.deviceWoT.produce(this.thingModel);
        console.log("Thing produced");

        this.thing = exposedThing;
        this.td = exposedThing.getThingDescription();
        this.initializeProperties(); // Initialize properties and add their handlers
        this.initializeActions(); // Initialize actions and add their handlers
        // Events do not need to be initialzed, can be emited from anywhere

        console.log(`Exposing Thing: ${this.thingModel.title}`);
        await this.thing.expose(); // Expose thing
        console.log("Exposed Thing");

        if (this.tdDirectory) {
            this.register(this.tdDirectory);
        }
        //  this.listenToMyEvent(); // used to listen to specific events provided by a library. If you don't have events, simply remove it
    }

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

    // private async myPropertyReadHandler(options?: WoT.InteractionOptions) {
    //     // read something
    //     return this.myProperty;
    // }
    // private async myPropertyWriteHandler(inputData: WoT.InteractionOutput, options?: WoT.InteractionOptions) {
    //     // write something to property
    //     this.myProperty = await inputData.value();
    // }

    // private myProperty: WoT.InteractionInput;
    // private propOilLevel: WoT.InteractionInput;
    // private propTyrePressure: WoT.InteractionInput;
    // private propMaintenanceNeeded: WoT.InteractionInput;
    // private propTotalMileage: WoT.InteractionInput;
    // private propNextServiceDistance: WoT.InteractionInput;
    // private propDoorStatus: WoT.InteractionInput;

    // ------------------------------------------------------------------------
    // Property Handlers
    // ------------------------------------------------------------------------
    
    // Oil level
    private async propOilLevelReadHandler(options?: WoT.InteractionOptions) {
        this.propOilLevel = this.readFromSensor("oilLevel");
        return this.propOilLevel;
    }

    // Tyre pressure
    private async propTyrePressureReadHandler(options?: WoT.InteractionOptions) {
        this.propOilLevel = this.readFromSensor("tyrePressure");
        return this.propTyrePressure;
    }

    // Maintenance needed
    private async propMaintenanceNeededReadHandler(options?: WoT.InteractionOptions) {
        if (this.varNextServiceDistance < 500) {
            this.propMaintenanceNeeded = true;
            this.varMaintenanceNedded = true;
            // Notify a "maintainer" when the value has changed
            // (the notify function here simply logs a message to the console)
            this.notify(
                "admin@leetfleet.com",
                `maintenanceNeeded property has changed, new value is: ${this.varMaintenanceNeddedHistory}`
            );
            if (this.varMaintenanceNeddedHistory != this.varMaintenanceNedded) {
                this.varMaintenanceNeddedHistory = this.varMaintenanceNedded;
                this.thing.emitPropertyChange("maintenanceNeeded");
            }
            this.thing.emitEvent("eventMaintenanceNeeded", `Maintenance needed! - next scheduled service is due.`);        
        }
        return this.propTyrePressure;
    }



    private async propMaintenanceNeededWriteHandler(inputData: WoT.InteractionOutput, options?: WoT.InteractionOptions) {
        this.propMaintenanceNeeded = await inputData.value();
    }


    // ------------------------------------------------------------------------
    // Action Handlers
    // ------------------------------------------------------------------------
    private async myActionHandler(inputData?: WoT.InteractionOutput, options?: WoT.InteractionOptions) {
        // do something with inputData if available
        let dataValue: string | number | boolean | object | WoT.DataSchemaValue[];
        if (inputData) {
            dataValue = await inputData.value();
        }

        if (dataValue) {
            this.thing.emitEvent("myEvent", null); // Emiting an event (may be removed; only for demonstration purposes)
        }

        let outputData = "";

        // resolve that with outputData if available, else resolve that action was successful without returning anything
        if (outputData) {
            return outputData;
        } else {
            return null;
        }
    }

    private listenToMyEvent() {
        ;
        /*
        specialLibrary.getMyEvent()//change specialLibrary to your library
        .then((thisEvent) => {
            this.thing.emitEvent("myEvent",""); //change quotes to your own event data
        });
        */
    }

    private initializeProperties() {
        // Property Oil Level
        this.propOilLevel = 100; // [%]; // replace quotes with the initial value
        this.thing.setPropertyReadHandler("oilLevel", this.propOilLevelReadHandler); // not applicable for write-only

        // Property Tyre Pressure
        this.propTyrePressure = 35; // [PSI]
        this.thing.setPropertyReadHandler("tyrePressure", this.propTyrePressureReadHandler); // not applicable for write-only

    }

    private initializeActions() {
        //fill in add actions
        this.thing.setActionHandler("myAction", async (inputData) => {
            let dataValue = await inputData.value();
            if (!ajv.validate(this.td.actions.myAction.input, dataValue)) {
                throw new Error("Invalid input");
            } else {
                return this.myActionHandler(inputData);
            }
        });
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

    private readMilometerServiceInterval() {
        return this.varNextServiceDistance;
    } 

    private readMilometer() {
        // Emulate mileage by increasing it randomly between 0 and 500 km
        let mileageIncrease;
        mileageIncrease = this.getRandomInt(0, 500);
        this.varTotalMileage += mileageIncrease;
        this.varNextServiceDistance -= mileageIncrease;
        console.log("Reading milometer: " + this.varTotalMileage);
        console.log("Distance left until next service is due: " + this.varNextServiceDistance);
        return
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
}