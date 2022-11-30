import * as WoT from "wot-typescript-definitions";
import request = require("request");

// Required steps to create a servient for a client
const { Servient, Helpers } = require("@node-wot/core");
const { HttpClientFactory } = require('@node-wot/binding-http');

export class ConsumedThing {
    public thing: WoT.ConsumedThing
    public td_url: string
    public servient = Servient
    public wotHelpers = Helpers

    constructor(td_url: string) {
        this.td_url = td_url;
        this.servient = new Servient();
        this.servient.addClientFactory(new HttpClientFactory(null));
        this.wotHelpers = new Helpers(this.servient);

        this.thing = ConsumedThing.tdConsume(this.td_url);

    }

    // Read with rhythm Doo-Doo-Doo-DooDoo-DooDoo
    // I-don't-know-ifthis-willwork!
    
    private static tdConsume(url: string): WoT.ConsumedThing {

        return request(url, function (error, response, body) {
            if (error) {
                console.log("Error occurred when making get request on td url...\n");
                console.log(error);
                return error; // Don't have an elegant way to handle this yet
            } else {
                console.log("Successful Request: " + response.statusCode);
                console.log("TD url was queried successfully\n");
                let json = JSON.parse(body);
                return WoT.consume(json);
            }
        });
    }
}