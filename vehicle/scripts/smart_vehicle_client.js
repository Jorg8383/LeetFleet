// client.js
// Required steps to create a servient for a client
const { Servient, Helpers } = require("@node-wot/core");
const { HttpClientFactory } = require('@node-wot/binding-http');

const servient = new Servient();
servient.addClientFactory(new HttpClientFactory(null));
const WoTHelpers = new Helpers(servient);

WoTHelpers.fetch("http://localhost:8080/Smart-Vehicle").then(async (td) => {
    try {
        servient.start().then(async (WoT) => {
            // Here we're conusming the thing
            try {
                const thing = await WoT.consume(td);
                log("Thing Description:", td);


            } catch (err) {
                console.error("Script error:", err);
            }        
        });
    }
    catch (err) {
        console.error("Script error:", err);
    }
}).catch((err) => { console.error("Fetch error:", err); });