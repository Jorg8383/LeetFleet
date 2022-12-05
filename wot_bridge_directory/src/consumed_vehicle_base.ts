import * as WoT from "wot-typescript-definitions";

export class WotConsumedDevice {
    public thing: WoT.ConsumedThing;
    public deviceWoT: typeof WoT;
    public td: WoT.ThingDescription;
    private tdUri: string;
    private wotHiveUri = "http://localhost:9000/api/things/";

    constructor(deviceWoT: typeof WoT, tdId?: string) {
        // initialze WotDevice parameters
        this.deviceWoT = deviceWoT;
        if (tdId) {
            this.tdUri = this.wotHiveUri + tdId;
        }
    }

    public async startDevice() {
        console.log("Consuming thing..." + this.tdUri);
        this.td = await this.retrieveTD();
        // console.log(this.td);
        const consumedThing = await this.deviceWoT.consume(this.td);
        console.log("Thing is now consumed with ID: " + this.td.id);

        this.thing = consumedThing;
        this.observeProperties(this.thing);
        this.subscribe(this.thing);

        return true;
    }

    private async retrieveTD() {
        try{
            const response = await fetch(this.tdUri);
            const body = await response.json();
            // console.log(body);
            return body;
        } catch (error) {
            console.error(error);
        }
    }

    private observeProperties(thing: WoT.ConsumedThing) {
        thing.observeProperty("propTotalMileage", async (data) => {
            console.log("Observed 'propTotalMileage' property has changed! New value is:", await data.value(), "-> Thing-ID: ", this.td.id);
        }).then();

        thing.observeProperty("propMaintenanceNeeded", async (data) => {
            console.log("Observed 'propMaintenanceNeeded' property has changed! New value is:", await data.value(), "-> Thing-ID: ", this.td.id);
        }).then();

        thing.observeProperty("propServiceDistance", async (data) => {
            console.log("Observed 'propServiceDistance' property has changed! New value is:", await data.value(), "-> Thing-ID: ", this.td.id);
        }).then();
    }

    private subscribe(thing: WoT.ConsumedThing) {
        thing.subscribeEvent("eventLowOnOil", async (data) => {
            console.log("eventLowOnOil:", await data.value(), "-> Thing-ID: ", this.td.id);
        }).then();
        thing.subscribeEvent("eventLowTyrePressure", async (data) => {
            console.log("eventLowTyrePressure:", await data.value(), "-> Thing-ID: ", this.td.id);
        }).then();
        thing.subscribeEvent("eventMaintenanceNeeded", async (data) => {
            console.log("eventMaintenanceNeeded:", await data.value(), "-> Thing-ID: ", this.td.id);
        }).then();
    }
}
