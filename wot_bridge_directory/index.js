//Where your concrete implementation is included
// WotDevice = require("./dist/base.js").WotDevice
WotDevice = require("./dist/consumed_vehicle_base.js").WotConsumedDevice

/*
This project supports the registration of the generated TD to a TD directory
Fill in the directory URI where the HTTP POST request to send the TD will be made
If you leave it empty, registration thread will never execute, otherwise it will try to register every 10 seconds 
*/
const TD_DIRECTORY = "http://localhost:9000/api/events/create?diff=true"


Servient = require("@node-wot/core").Servient
//Importing the required bindings
HttpServer = require("@node-wot/binding-http").HttpServer

//Creating the instances of the binding servers
var httpServer = new HttpServer({port: 8080});

//Building the servient object
var servient = new Servient();
//Adding different bindings to the server
servient.addServer(httpServer);

const deviceId = "http://localhost:9000/api/things/urn:uuid:13b5122b-ac41-452f-a72b-58b969e6a8cc";

servient.start().then((WoT) => {
    wotDevice = new WotDevice(WoT, deviceId); // TODO change the wotDevice to something that makes more sense
    wotDevice.startDevice();
});

// const sseSource = new EventSource("http://localhost:9000/api/events/create?diff=true");

// sseSource.onmessage = function (event) {
//     const { t } = JSON.parse(event.data);
//     console.log(t);
// }

