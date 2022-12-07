import * as WoT from "wot-typescript-definitions";

export class WotConsumedDevice {
    public thing: WoT.ConsumedThing;
    public deviceWoT: typeof WoT;
    public td: WoT.ThingDescription;
    private tdUri: string;
    private wotHiveUri = "http://localhost:9000/api/things/";

    private vehicleJSON = {vehicleID : "WoT-ID-Mfr-VIN",
                            fleetManager : "N/A",
                            tdURL : "http://localhost:8080/",
                            oilLevel : 50,
                            tyrePressure : 30,
                            mileage : 10000,
                            nextServiceDistance : 10000,
                            doorStatus : "LOCKED",
                            maintenanceNeeded : false}

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
        console.log("JSON representation is currently:");
        console.log(JSON.stringify(this.vehicleJSON));
        await this.initialiseJSON(this.vehicleJSON);
        console.log("JSON representation is now:");
        console.log(JSON.stringify(this.vehicleJSON));
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

    private async initialiseJSON(json) {
        const allData = await this.thing.readAllProperties();
        json.vehicleID = this.updateVehicleID(json.vehicleID);
        json.tdURL = json.tdURL + this.thing.getThingDescription().title;
        json.oilLevel = await allData.get('propOilLevel').value();
        json.tyrePressure = await allData.get('propTyrePressure').value();
        json.mileage = await allData.get('propTotalMileage').value();
        json.nextServiceDistance = await allData.get('propServiceDistance').value();
        json.doorStatus = await allData.get('propDoorStatus').value();
        json.maintenanceNeeded = await allData.get('propMaintenanceNeeded').value();
    }

    private updateVehicleID(vehicleID:string):string {
        let randomNum = this.randomInt(1, 9999);
        let randomNumString = "";
        if (randomNum / 10 < 1) {
            randomNumString = "000" + randomNum;
        } else if (randomNum / 10 < 10) {
            randomNumString = "00" + randomNum;
        } else if (randomNum / 10 < 100) {
            randomNumString = "0" + randomNum;
        } else {
            randomNumString = randomNum as unknown as string;
        }
        return vehicleID + "-" + randomNumString
    }

    private randomInt(min: number, max: number):number {
        min = Math.ceil(min)
        max = Math.floor(max)
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }

    private observeProperties(thing: WoT.ConsumedThing) {
        thing.observeProperty("propTotalMileage", async (data) => {
            // @ts-ignore
            this.vehicleJSON.mileage = await data.value();
        });

        thing.observeProperty("propMaintenanceNeeded", async (data) => {
            // @ts-ignore
            this.vehicleJSON.maintenanceNeeded = await data.value();
        });

        thing.observeProperty("propServiceDistance", async (data) => {
            // @ts-ignore
            this.vehicleJSON.nextServiceDistance = await data.value();
        });

        thing.observeProperty("propDoorStatus", async (data) => {
            // @ts-ignore
            this.vehicleJSON.doorStatus = await data.value();
        })
    }

    private subscribe(thing: WoT.ConsumedThing) {
        thing.subscribeEvent("eventLowOnOil", async (data) => {
            console.log("eventLowOnOil:", await data.value(), "-> Thing-ID: ", this.td.id);
        });
        thing.subscribeEvent("eventLowTyrePressure", async (data) => {
            console.log("eventLowTyrePressure:", await data.value(), "-> Thing-ID: ", this.td.id);
        });
        thing.subscribeEvent("eventMaintenanceNeeded", async (data) => {
            console.log("eventMaintenanceNeeded:", await data.value(), "-> Thing-ID: ", this.td.id);
        });
    }
}
