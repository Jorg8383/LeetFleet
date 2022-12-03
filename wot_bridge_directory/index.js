//Where your concrete implementation is included
// WotDevice = require("./dist/base.js").WotDevice
WotDevice = require("./dist/consumed_vehicle_base.js").WotConsumedDevice
const { HttpClientFactory } = require('@node-wot/binding-http');
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
var httpServer = new HttpServer({port: 8090});

//Building the servient object
var servient = new Servient();
servient.addClientFactory(new HttpClientFactory(null));
//Adding different bindings to the server
servient.addServer(httpServer);

// const deviceId = "urn:uuid:13b5122b-ac41-452f-a72b-58b969e6a8cc";
const testingURL = "http://localhost:8080/smart-vehicle";

// servient.start().then((WoT) => {
//     wotDevice = new WotDevice(WoT, testingURL); // TODO change the wotDevice to something that makes more sense
//     wotDevice.startDevice();
// });

var EventSource = require("eventsource");
const sseDirectory = new EventSource("http://localhost:9000/api/events/create?diff=true");

var printWaitMessage = true;
while (true) {
    if (printWaitMessage) {
        console.log("Waiting for an event 'thing_created'...");
        printWaitMessage = false;
    }

    sseDirectory.onopen = function(e) {
        console.log("Event open");
    }

    sseDirectory.onerror = function(e) {
        console.log("Event error");
        if (this.readyState == sseDirectory.CONNECTING) {
            console.log(`Reconnecting (readyState=${this.readyState})...`);
        } else {
            console.log("An error has occured!");
        }
    }

    sseDirectory.onmessage = function(e) {
        console.log("Event onMessage received");
        const { t } = JSON.parse(e.data);
        console.log(t);
        printWaitMessage = true;

    }

    // sseSource.addEventListener('create', function (e) {
    //     console.log("OnMessage...")
    //     const { t } = JSON.parse(e.data);
    //     console.log(t);
    //     printWaitMessage = true;
       
    // });
}

