// This consumed thing program is essentially the representation of
// an exposed thing digitally. Given the example use case of this
// system to manage smart vehicles, the exposed thing would technically
// be an actual vehicle (but in this case is a containerised piece of
// software). The consumed thing is then the digital representation of
// that physical exposed thing. This digital representation that is here
// then communicates over the web to our Akka system to update Akka with the
// current representation of the vehicle with the accompanying JSON

// This import provides the support for developing class for
// consumed things in typescript
import * as WoT from "wot-typescript-definitions";

// Overall class description for consumed things for this project
export class WotConsumedDevice {
    public thing: WoT.ConsumedThing;
    public deviceWoT: typeof WoT;
    public td: WoT.ThingDescription;
    private tdUri: string;
    private wotHiveUri = "http://leetfleet-wothive-1:9000/api/things/";

    // Default JSON representation of a vehicle
    private vehicleJSON = {"vehicleId" : "WoT-ID-Mfr-VIN-0000",
                            "fleetManager" : "N/A",
                            "tdURL" : "http://localhost:8081/",
                            "oilLevel" : 50,
                            "tyrePressure" : 30,
                            "mileage" : 10000,
                            "nextServiceDistance" : 10000,
                            "doorStatus" : "LOCKED",
                            "maintenanceNeeded" : false}

    constructor(deviceWoT: typeof WoT, tdId?: string) {
        // initialze WotDevice parameters
        this.deviceWoT = deviceWoT;
        if (tdId) {
            this.tdUri = this.wotHiveUri + tdId;
        }
    }

    // Start up method that consumes an exposed thing and has the
    // consumed thing observe properties and events
    public async startDevice() {
        this.td = await this.retrieveTD();
        const consumedThing = await this.deviceWoT.consume(this.td);

        this.thing = consumedThing;
        await this.initialiseJSON(this.vehicleJSON);

        this.updateAkka();
        this.observeProperties(this.thing);
        this.subscribe(this.thing);

        return true;
    }

    // Method to retrieve the thing description for the exposed thing
    // that has been consumed
    private async retrieveTD() {
        try{
            const response = await fetch(this.tdUri);
            const body = await response.json();
            return body;
        } catch (error) {
            console.error(error);
        }
    }

    // Method that handles the first update of the JSON representation
    // of the vehicle being passed to Akka
    private async initialiseJSON(json) {
        console.log("InitialiseJSON called:\n\n");
        const url = this.td.forms[0].href;
        console.log("URL constant is: " + url);
        const allData = await this.thing.readAllProperties();
        json["vehicleId"] = this.td.title;
        json["tdURL"] = url.replace("properties", "");
        console.log("Updated URL is now: " + this.vehicleJSON.tdURL);
        json["oilLevel"] = await allData.get('propOilLevel').value();
        json["tyrePressure"] = await allData.get('propTyrePressure').value();
        json["mileage"] = await allData.get('propTotalMileage').value();
        json["nextServiceDistance"] = await allData.get('propServiceDistance').value();
        json["doorStatus"] = await allData.get('propDoorStatus').value();
        json["maintenanceNeeded"] = await allData.get('propMaintenanceNeeded').value();
        console.log("JSON representation is:");
        console.log(this.vehicleJSON);
        console.log("\n\n");
    }

    // Method that handles observing changes in each property in the
    // exposed thing, update the JSON representation of that vehicle
    // and then communicate this to Akka
    private observeProperties(thing: WoT.ConsumedThing) {
        thing.observeProperty("propTotalMileage", async (data) => {
            // @ts-ignore
            this.vehicleJSON["mileage"] = await data.value();
            this.updateAkka();
        });

        thing.observeProperty("propMaintenanceNeeded", async (data) => {
            // @ts-ignore
            this.vehicleJSON["maintenanceNeeded"] = await data.value();
            this.updateAkka();
        });

        thing.observeProperty("propServiceDistance", async (data) => {
            // @ts-ignore
            this.vehicleJSON["nextServiceDistance"] = await data.value();
            this.updateAkka();
        });

        thing.observeProperty("propDoorStatus", async (data) => {
            // @ts-ignore
            this.vehicleJSON["doorStatus"] = await data.value();
            this.updateAkka();
        })
    }

    // Method to subscribe to the events of the consumed thing
    private subscribe(thing: WoT.ConsumedThing) {
        thing.subscribeEvent("eventLowOnOil", async (data) => {
            console.log("eventLowOnOil:", await data.value(), "-> Thing-ID: ", this.td.id);
            fetch("http://webportal:8080/wot", {
                method: 'POST',
                headers: {
                    "Content-type" : "application/json"
                },
                body: "Event message - vehicle " + this.vehicleJSON.vehicleId
                    + " is low on oil"
            }).then(res => res.json()).then(res => {
                console.log(res);
            }).catch(err => {
                console.log(err);
            })
        });
        thing.subscribeEvent("eventLowTyrePressure", async (data) => {
            console.log("eventLowTyrePressure:", await data.value(), "-> Thing-ID: ", this.td.id);
            fetch("http://webportal:8080/wot", {
                method: 'POST',
                headers: {
                    "Content-type" : "application/json"
                },
                body: "Event message - vehicle " + this.vehicleJSON.vehicleId
                    + " has low tyre pressure"
            }).then(res => res.json()).then(res => {
                console.log(res);
            }).catch(err => {
                console.log(err);
            })
        });
        thing.subscribeEvent("eventMaintenanceNeeded", async (data) => {
            console.log("eventMaintenanceNeeded:", await data.value(), "-> Thing-ID: ", this.td.id);
            fetch("http://webportal:8080/wot", {
                method: 'POST',
                headers: {
                    "Content-type" : "application/json"
                },
                body: "Event message - vehicle " + this.vehicleJSON.vehicleId
                    + " requires maintenance"
            }).then(res => res.json()).then(res => {
                console.log(res);
            }).catch(err => {
                console.log(err);
            })
        });
    }

    // Extracted method to handle sending JSON representation of vehicle
    // to Actor system
    private updateAkka() {
        fetch("http://webportal:8080/wot", {
            method: 'POST',
            headers: {
                "Content-type" : "application/json"
            },
            body: JSON.stringify({
                "vehicleId" : this.vehicleJSON.vehicleId,
                "fleetManager" : this.vehicleJSON.fleetManager,
                "tdURL" : this.vehicleJSON.tdURL,
                "oilLevel" : this.vehicleJSON.oilLevel,
                "tyrePressure" : this.vehicleJSON.tyrePressure,
                "mileage" : this.vehicleJSON.mileage,
                "nextServiceDistance" : this.vehicleJSON.nextServiceDistance,
                "doorStatus" : this.vehicleJSON.doorStatus,
                "maintenanceNeeded" : this.vehicleJSON.maintenanceNeeded})
        }).then(res => {
            if (res.status >= 300) {
                throw new Error("There was an error with the request: " + res.status)
            }
            res.json()
        }).then(res => {
            console.log(res);
        }).catch(err => {
            console.log(err);
        })
    }
}
