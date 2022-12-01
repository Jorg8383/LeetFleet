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
exports.ConsumedVehicle = void 0;
const WoT = require("wot-typescript-definitions");
const request = require("request");
// Required steps to create a servient for a client
const { Servient, Helpers } = require("@node-wot/core");
const { HttpClientFactory } = require('@node-wot/binding-http');
class ConsumedVehicle {
    constructor(td_url) {
        this.servient = Servient;
        this.wotHelpers = Helpers;
        this.td_url = td_url;
        this.servient = new Servient();
        this.servient.addClientFactory(new HttpClientFactory(null));
        this.wotHelpers = new Helpers(this.servient);
        this.thing = ConsumedVehicle.tdConsume(this.td_url);
        this.observeProperties(this.thing);
        this.subscribe(this.thing);
    }
    // Read with rhythm Doo-Doo-Doo-DooDoo-DooDoo
    // I-don't-know-ifthis-willwork!
    static tdConsume(url) {
        return request(url, function (error, response, body) {
            if (error) {
                console.log("Error occurred when making get request on td url...\n");
                console.log(error);
                return error; // Don't have an elegant way to handle this yet
            }
            else {
                console.log("Successful Request: " + response.statusCode);
                console.log("TD url was queried successfully\n");
                let json = JSON.parse(body);
                return WoT.consume(json);
            }
        });
    }
    observeProperties(thing) {
        thing.observeProperty("totalMileage", (data) => __awaiter(this, void 0, void 0, function* () {
            console.log("Observed 'totalMileage' property has changed! New value is:", yield data.value());
        })).then(
        // Not sure what the promise here is but something happens...
        );
        thing.observeProperty("maintenanceNeeded", (data) => __awaiter(this, void 0, void 0, function* () {
            console.log("Observed 'maintenanceNeeded' property has changed! New value is:", yield data.value());
        })).then(
        // Not sure what the promise here is but something happens...
        );
        thing.observeProperty("nextServiceDistance", (data) => __awaiter(this, void 0, void 0, function* () {
            console.log("Observed 'nextServiceDistance' property has changed! New value is:", yield data.value());
        })).then(
        // Not sure what the promise here is but something happens...
        );
    }
    subscribe(thing) {
        thing.subscribeEvent("eventLowOnOil", (data) => __awaiter(this, void 0, void 0, function* () {
            console.log("eventLowOnOil:", yield data.value());
        })).then(
        // Still don't know what the promises do here...
        // Sing it with me now...
        // I-don't-know-ifthis--willwork
        );
        thing.subscribeEvent("eventLowTyrePressure", (data) => __awaiter(this, void 0, void 0, function* () {
            console.log("eventLowTyrePressure:", yield data.value());
        })).then();
        thing.subscribeEvent("eventMaintenanceNeeded", (data) => __awaiter(this, void 0, void 0, function* () {
            console.log("eventMaintenanceNeeded:", yield data.value());
        })).then();
    }
}
exports.ConsumedVehicle = ConsumedVehicle;
//# sourceMappingURL=consumed_vehicle_base.js.map