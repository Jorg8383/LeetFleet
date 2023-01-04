//Where your concrete implementation is included
// WotDevice = require("./dist/base.js").WotDevice
SmartVehicle = require("./dist/smart_vehicle_base.js").WotDevice
// SmartVehicle2 = require("./dist/smart_vehicle_base2.js").WotDevice

/*
This project supports the registration of the generated TD to a TD directory
Fill in the directory URI where the HTTP POST request to send the TD will be made
If you leave it empty, registration thread will never execute, otherwise it will try to register every 10 seconds
*/
// const TD_DIRECTORY = "http://0.0.0.0:9000/api/things/"
//const TD_DIRECTORY = "http://triple-store-wothive-1:9000/api/things/"
const TD_DIRECTORY = "http://" + process.env.HOST_WOT_DIR_SERVICE + ":9000/api/things/"

Servient = require("@node-wot/core").Servient
//Importing the required bindings
HttpServer = require("@node-wot/binding-http").HttpServer

//Creating the instances of the binding servers
var httpServer = new HttpServer({port: process.env.ENV_PORT});
// var httpServer2 = new HttpServer({port: 8081});

//Building the servient object
var servient = new Servient();
//Adding different bindings to the server
servient.addServer(httpServer);

servient.start().then((WoT) => {
    console.log("Starting new device on port " + process.env.ENV_PORT +
            " with vehicle number " + process.env.ENV_VEHICLE_NUMBER);
    SmartVehicle = new SmartVehicle(WoT, TD_DIRECTORY,
        process.env.ENV_VEHICLE_NUMBER, process.env.HOSTNAME);
    // SmartVehicle1 = new SmartVehicle1(WoT);
    SmartVehicle.startDevice();
    SmartVehicle.emulateDevice(process.env.VEHICLE_SIM_INTERVAL);
});



// //Where your concrete implementation is included
// // WotDevice = require("./dist/base.js").WotDevice
// SmartVehicle = require("./dist/smart_vehicle_base.js").WotDevice
// // SmartVehicle2 = require("./dist/smart_vehicle_base2.js").WotDevice

// /*
// This project supports the registration of the generated TD to a TD directory
// Fill in the directory URI where the HTTP POST request to send the TD will be made
// If you leave it empty, registration thread will never execute, otherwise it will try to register every 10 seconds
// */
// const TD_DIRECTORY = "http://0.0.0.0:9000/api/things/"

// Servient = require("@node-wot/core").Servient
// //Importing the required bindings
// HttpServer = require("@node-wot/binding-http").HttpServer

// //Creating the instances of the binding servers
// var httpServer1 = new HttpServer({port: 8080});
// var httpServer2 = new HttpServer({port: 8081});

// //Building the servient object
// var servient1 = new Servient();
// var servient2 = new Servient();
// //Adding different bindings to the server
// servient1.addServer(httpServer1);
// servient2.addServer(httpServer2);

// servient1.start().then((WoT) => {
//     let vehicleNumber = "1";
//     SmartVehicle1 = new SmartVehicle(WoT, TD_DIRECTORY, vehicleNumber);
//     // SmartVehicle1 = new SmartVehicle1(WoT);
//     SmartVehicle1.vehicleNumber = 1;
//     SmartVehicle1.startDevice();
//     SmartVehicle1.emulateDevice();
// });

// servient2.start().then((WoT) => {
//     let vehicleNumber = "2";
//     SmartVehicle2 = new SmartVehicle(WoT, TD_DIRECTORY, vehicleNumber);
//     // SmartVehicle1 = new SmartVehicle1(WoT);
//     SmartVehicle2.vehicleNumber = 2;
//     SmartVehicle2.startDevice();
//     SmartVehicle2.emulateDevice();
// });
