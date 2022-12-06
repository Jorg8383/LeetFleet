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
exports.WotConsumedDevice = void 0;
class WotConsumedDevice {
    constructor(deviceWoT, tdId) {
        this.wotHiveUri = "http://localhost:9000/api/things/";
        this.vehicleJSON = { vehicleID: "WoT-ID-Mfr-VIN",
            fleetManager: "N/A",
            tdURL: "http://localhost:8080/",
            oilLevel: 50,
            tyrePressure: 30,
            mileage: 10000,
            nextServiceDistance: 10000,
            doorStatus: "LOCKED",
            maintenanceNeeded: false };
        // initialze WotDevice parameters
        this.deviceWoT = deviceWoT;
        if (tdId) {
            this.tdUri = this.wotHiveUri + tdId;
        }
    }
    startDevice() {
        return __awaiter(this, void 0, void 0, function* () {
            console.log("Consuming thing..." + this.tdUri);
            this.td = yield this.retrieveTD();
            // console.log(this.td);
            const consumedThing = yield this.deviceWoT.consume(this.td);
            console.log("Thing is now consumed with ID: " + this.td.id);
            this.thing = consumedThing;
            console.log("JSON representation is currently:");
            console.log(JSON.stringify(this.vehicleJSON));
            this.vehicleJSON = yield this.initialiseJSON(this.vehicleJSON);
            console.log("JSON representation is now:");
            console.log(JSON.stringify(this.vehicleJSON));
            this.observeProperties(this.thing);
            this.subscribe(this.thing);
            return true;
        });
    }
    retrieveTD() {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const response = yield fetch(this.tdUri);
                const body = yield response.json();
                // console.log(body);
                return body;
            }
            catch (error) {
                console.error(error);
            }
        });
    }
    initialiseJSON(json) {
        return __awaiter(this, void 0, void 0, function* () {
            json.vehicleID = this.updateVehicleID(json.vehicleID);
            json.tdURL = json.tdURL + this.thing.getThingDescription().title;
            let oil = 0;
            yield this.thing.readProperty("propOilLevel")
                .then((data) => {
                data.value().then(value => {
                    oil = value;
                    console.log(oil);
                    json.oilLevel = oil;
                });
            });
            console.log(oil);
            console.log("JSON oil level:", json.oilLevel);
            return json;
        });
    }
    updateVehicleID(vehicleID) {
        let randomNum = this.randomInt(1, 9999);
        let randomNumString = "";
        if (randomNum / 10 < 1) {
            randomNumString = "000" + randomNum;
        }
        else if (randomNum / 10 < 10) {
            randomNumString = "00" + randomNum;
        }
        else if (randomNum / 10 < 100) {
            randomNumString = "0" + randomNum;
        }
        else {
            randomNumString = randomNum;
        }
        return vehicleID + "-" + randomNumString;
    }
    randomInt(min, max) {
        min = Math.ceil(min);
        max = Math.floor(max);
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }
    observeProperties(thing) {
        thing.observeProperty("propTotalMileage", (data) => __awaiter(this, void 0, void 0, function* () {
            console.log("Observed 'propTotalMileage' property has changed! New value is:", yield data.value(), "-> Thing-ID: ", this.td.id);
        })).then();
        thing.observeProperty("propMaintenanceNeeded", (data) => __awaiter(this, void 0, void 0, function* () {
            console.log("Observed 'propMaintenanceNeeded' property has changed! New value is:", yield data.value(), "-> Thing-ID: ", this.td.id);
        })).then();
        thing.observeProperty("propServiceDistance", (data) => __awaiter(this, void 0, void 0, function* () {
            console.log("Observed 'propServiceDistance' property has changed! New value is:", yield data.value(), "-> Thing-ID: ", this.td.id);
        })).then();
    }
    subscribe(thing) {
        thing.subscribeEvent("eventLowOnOil", (data) => __awaiter(this, void 0, void 0, function* () {
            console.log("eventLowOnOil:", yield data.value(), "-> Thing-ID: ", this.td.id);
        })).then();
        thing.subscribeEvent("eventLowTyrePressure", (data) => __awaiter(this, void 0, void 0, function* () {
            console.log("eventLowTyrePressure:", yield data.value(), "-> Thing-ID: ", this.td.id);
        })).then();
        thing.subscribeEvent("eventMaintenanceNeeded", (data) => __awaiter(this, void 0, void 0, function* () {
            console.log("eventMaintenanceNeeded:", yield data.value(), "-> Thing-ID: ", this.td.id);
        })).then();
    }
}
exports.WotConsumedDevice = WotConsumedDevice;
//# sourceMappingURL=consumed_vehicle_base.js.map