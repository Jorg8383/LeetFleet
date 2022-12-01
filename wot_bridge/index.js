//Where your concrete implementation is included
// WotDevice = require("./dist/base.js").WotDevice
const ConsumedVehicle = require("./dist/consumed_vehicle_base");
// const {ConsumedThing} = require("./dist/consumed_vehicle_base");
// ConsumedThing = require("./dist/consumed_vehicle_base.js").ConsumedThing

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
var httpServer = new HttpServer({port: 10000});

//Building the servient object
var servient = new Servient();
//Adding different bindings to the server
servient.addServer(httpServer);

servient.start().then((WoT) => {
    let consumedThing = new ConsumedVehicle.ConsumedVehicle("http://localhost:8080/smart-vehicle"); // TODO change the wotDevice to something that makes more sense
});