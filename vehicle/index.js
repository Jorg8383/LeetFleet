//Where your concrete implementation is included
// WotDevice = require("./dist/base.js").WotDevice
WotDevice = require("./dist/smart_vehicle_base.js").WotDevice

/*
This project supports the registration of the generated TD to a TD directory
Fill in the directory URI where the HTTP POST request to send the TD will be made
If you leave it empty, registration thread will never execute, otherwise it will try to register every 10 seconds 
*/
const TD_DIRECTORY = "http://localhost:9000:/api/td"


Servient = require("@node-wot/core").Servient
//Importing the required bindings
HttpServer = require("@node-wot/binding-http").HttpServer

//Creating the instances of the binding servers
var httpServer = new HttpServer({port: 8080});

//Building the servient object
var servient = new Servient();
//Adding different bindings to the server
servient.addServer(httpServer);

servient.start().then((WoT) => {
    // wotDevice = new WotDevice(WoT, TD_DIRECTORY); // TODO change the wotDevice to something that makes more sense
    wotDevice = new WotDevice(WoT); // TODO change the wotDevice to something that makes more sense
    wotDevice.startDevice();
    wotDevice.emulate();
});
