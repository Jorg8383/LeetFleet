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