"use strict";
// This consumed thing program is essentially the representation of
// an exposed thing digitally. Given the example use case of this
// system to manage smart vehicles, the exposed thing would technically
// be an actual vehicle (but in this case is a containerised piece of
// software). The consumed thing is then the digital representation of
// that physical exposed thing. This digital representation that is here
// then communicates over the web to our Akka system to update Akka with the
// current representation of the vehicle with the accompanying JSON
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
exports.WotConsumedDevice = void 0;
// Overall class description for consumed things for this project
class WotConsumedDevice {
    constructor(deviceWoT, tdId) {
        this.wotHiveUri = "http://leetfleet-wothive-1:9000/api/things/";
        // Default JSON representation of a vehicle
        this.vehicleJSON = { "vehicleId": "WoT-ID-Mfr-VIN-0000",
            "fleetManager": "N/A",
            "tdURL": "http://localhost:8081/",
            "oilLevel": 50,
            "tyrePressure": 30,
            "mileage": 10000,
            "nextServiceDistance": 10000,
            "doorStatus": "LOCKED",
            "maintenanceNeeded": false };
        // initialze WotDevice parameters
        this.deviceWoT = deviceWoT;
        if (tdId) {
            this.tdUri = this.wotHiveUri + tdId;
        }
    }
    // Start up method that consumes an exposed thing and has the
    // consumed thing observe properties and events
    startDevice() {
        return __awaiter(this, void 0, void 0, function* () {
            this.td = yield this.retrieveTD();
            const consumedThing = yield this.deviceWoT.consume(this.td);
            this.thing = consumedThing;
            yield this.initialiseJSON(this.vehicleJSON);
            this.updateAkka();
            this.observeProperties(this.thing);
            this.subscribe(this.thing);
            return true;
        });
    }
    // Method to retrieve the thing description for the exposed thing
    // that has been consumed
    retrieveTD() {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const response = yield fetch(this.tdUri);
                const body = yield response.json();
                return body;
            }
            catch (error) {
                console.error(error);
            }
        });
    }
    // Method that handles the first update of the JSON representation
    // of the vehicle being passed to Akka
    initialiseJSON(json) {
        return __awaiter(this, void 0, void 0, function* () {
            const url = this.td.forms[0].href;
            const allData = yield this.thing.readAllProperties();
            json["vehicleId"] = this.td.title;
            json["tdURL"] = url.replace("properties", "");
            json["oilLevel"] = yield allData.get('propOilLevel').value();
            json["tyrePressure"] = yield allData.get('propTyrePressure').value();
            json["mileage"] = yield allData.get('propTotalMileage').value();
            json["nextServiceDistance"] = yield allData.get('propServiceDistance').value();
            json["doorStatus"] = yield allData.get('propDoorStatus').value();
            json["maintenanceNeeded"] = yield allData.get('propMaintenanceNeeded').value();
            console.log("JSON representation for " + this.td.id + " is:");
            console.log(this.vehicleJSON);
        });
    }
    // Method that handles observing changes in each property in the
    // exposed thing, update the JSON representation of that vehicle
    // and then communicate this to Akka
    observeProperties(thing) {
        thing.observeProperty("propTotalMileage", (data) => __awaiter(this, void 0, void 0, function* () {
            // @ts-ignore
            this.vehicleJSON["mileage"] = yield data.value();
            this.updateAkka();
        }));
        thing.observeProperty("propMaintenanceNeeded", (data) => __awaiter(this, void 0, void 0, function* () {
            // @ts-ignore
            this.vehicleJSON["maintenanceNeeded"] = yield data.value();
            this.updateAkka();
        }));
        thing.observeProperty("propServiceDistance", (data) => __awaiter(this, void 0, void 0, function* () {
            // @ts-ignore
            this.vehicleJSON["nextServiceDistance"] = yield data.value();
            this.updateAkka();
        }));
        thing.observeProperty("propDoorStatus", (data) => __awaiter(this, void 0, void 0, function* () {
            // @ts-ignore
            this.vehicleJSON["doorStatus"] = yield data.value();
            this.updateAkka();
        }));
    }
    // Method to subscribe to the events of the consumed thing
    subscribe(thing) {
        thing.subscribeEvent("eventLowOnOil", (data) => __awaiter(this, void 0, void 0, function* () {
            fetch("http://webportal:8080/wot", {
                method: 'POST',
                headers: {
                    "Content-type": "application/json"
                },
                body: "Event message - vehicle " + this.vehicleJSON.vehicleId
                    + " is low on oil"
            }).then(res => {
                console.log("Vehicle " + this.vehicleJSON.vehicleId + " is low on oil");
            }).catch(err => {
                console.log(err);
            });
        }));
        thing.subscribeEvent("eventLowTyrePressure", (data) => __awaiter(this, void 0, void 0, function* () {
            fetch("http://webportal:8080/wot", {
                method: 'POST',
                headers: {
                    "Content-type": "application/json"
                },
                body: "Event message - vehicle " + this.vehicleJSON.vehicleId
                    + " has low tyre pressure"
            }).then(res => {
                console.log("Vehicle " + this.vehicleJSON.vehicleId + " has low tyre pressure");
            }).catch(err => {
                console.log(err);
            });
        }));
        thing.subscribeEvent("eventMaintenanceNeeded", (data) => __awaiter(this, void 0, void 0, function* () {
            fetch("http://webportal:8080/wot", {
                method: 'POST',
                headers: {
                    "Content-type": "application/json"
                },
                body: "Event message - vehicle " + this.vehicleJSON.vehicleId
                    + " requires maintenance"
            }).then(res => {
                console.log("Vehicle " + this.vehicleJSON.vehicleId + " requires maintenance");
            }).catch(err => {
                console.log(err);
            });
        }));
    }
    // Extracted method to handle sending JSON representation of vehicle
    // to Actor system
    updateAkka() {
        fetch("http://webportal:8080/wot", {
            method: 'POST',
            headers: {
                "Content-type": "application/json"
            },
            body: JSON.stringify({
                "vehicleId": this.vehicleJSON.vehicleId,
                "fleetManager": this.vehicleJSON.fleetManager,
                "tdURL": this.vehicleJSON.tdURL,
                "oilLevel": this.vehicleJSON.oilLevel,
                "tyrePressure": this.vehicleJSON.tyrePressure,
                "mileage": this.vehicleJSON.mileage,
                "nextServiceDistance": this.vehicleJSON.nextServiceDistance,
                "doorStatus": this.vehicleJSON.doorStatus,
                "maintenanceNeeded": this.vehicleJSON.maintenanceNeeded
            })
        }).then(res => {
            if (res.status >= 300) {
                throw new Error("There was an error with the request: " + res.status);
            }
            // console.log(res.json());
        }).catch(err => {
            console.log(err);
        });
    }
}
exports.WotConsumedDevice = WotConsumedDevice;
//# sourceMappingURL=consumed_vehicle_base.js.map