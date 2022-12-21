/********************************************************************************************
                                    The WoT-Bridge
********************************************************************************************
Author: Jörg Striebel

The WoT-Hive, which is an implementation of a W3C Web of Things directory, forms the backbone 
of our bridge between the Web of Things world and the backend AKKA part. It allows for 
Exposed-Things to register themselves with this directory and for the bridge to retrieve the
Thing Descriptions (TD) of these Exposed-Things either individually or as a list of TDs. 
The WoT-Hive API offers CRUD methods to modify its configuration, register Exposed-Things,
retrieve individual or the entire list of registered TDs, and to subscribe to so-called 
Server-Sent-Events (SSE). The SSE would allow to notify a client which has subscribed to 
specific events, whenever one of these events is triggered. For instance, a client that has
registered itself for a “thing created” event, would be notified every time a new 
Exposed-Things has been registered. So far, that is the theory.

However, in practise it turned out that the SSE API had not been fully functional at the
time of testing it end of November 2022. This WoT-Hive project was launched at a university
in Madrid and released in early 2022. Judging by the commit history this project still seemed
to be in progress, potentially not yet fully implemented but certainly lacking comprehensive
documentation. So, when trying to subscribe to the “created” event, the server would return
with a HTTP 404 error, indicating that unlike outlined in the description of the WoT-Hive API,
this feature may had not been implemented yet. The code for the SSE client implementation 
can be found commented out at the end of this file.

Consequently, we had to come up with a workaround. Instead of having the bridge notified 
through Server-Sent-Events every time a new Exposed-Thing is registered, the bridge is now 
sending requests to the server at regular intervals (polling) for retrieving the entire 
list of registered TDs. In theory it would be possible to define an expiry date for 
registered TDs. However, since we are polling the directory now actively, we need to ensure
that only currently Exposed-Things are registered with the directory as only those can be 
consumed by its counterpart called “Consumed-Thing”. 

To realise this, the functionality of the bridge can be summarised as follows:
-	Fetching a list of TDs from the WoT-Hive directory at regular intervals 
    (currently every 10 sec)
-	Checking the fetched cache for relevant TD entries
        o	Keeping track of outdated TDs to be deleted
        o	Keeping track of current TDs to be consumed
        o	Keeping track of TDs that have already be consumed
-	Cleaning up the WoT-Hive directory by deleting outdated TDs. Therefore, keeping the 
    amount of data that must be exchanged to a minimum
-	Creating a so-called “Consumed-Thing”
    o	For each TD to be consumed a so-called “Servient” is instantiated
    o	For each TD to be consumed a HTTP server with its individual port is being created
    o	A client-factory and the HTTP server is than added to the servient
-	Finally, the servient spins up a Consumed-Thing for each TD
 
For more information about the WoT-Hive directory, check out:
https://github.com/oeg-upm/wot-hive
********************************************************************************************/

// const URI_API_THINGS = "http://localhost:9000/api/things";
const URI_API_THINGS = "http://triple-store-wothive-1:9000/api/things";
const FIRST_HTTP_SERVER_PORT = process.env.ENV_FIRST_SERVER_PORT;
const wotBridgeTimeStarted = Date.now();

let wotHiveLocalTdCache = {};
const wotIsConsumedThingDict = new Map();
const wotToBeConsumedThingDict = new Map();
const wotToBeDeletedThingDict = new Map();

var httpServerPortCount = 0;


// This function is to fetch all TDs listed in the WoT-Hive directory
async function fetchWotHiveTdCache() {
    try {
        // Fetch all listed TDs in the WoT-Hive directory
        const response = await fetch(URI_API_THINGS);
        // Check if response status is ok (200 < status < 300)
        if (!response.ok) {
            const message = `An error has occured while fetching: ${response.status}`;
            throw new Error(message); 
        }
        // Resolve response by converting the received data into JSON
        const tdCache = await response.json();
        return tdCache;
    } catch (error) {
        console.error(error);
    }
}

// This function is to find all current (newly created) and all outdated TD entries.
// It stores the "id" of all current and outdated TDs from the WoT-Hive dictionary
// in the dictionaries "wotToBeConsumedThingDict" and "wotToBeDeletedThingDict" respectively.
function checkCacheForRelevantEntries(cache) {
    console.log("The WoT-Hive cache contains " + cache.length + " TD entries.");
    if (cache.length != 0) {
        let deleteIdKey = 0;
        for (let i=0; i < cache.length; i++) {
            // Get one single TD and convert "created" time into a UTC timestamp
            let tdEntry = cache[i];
            const dt = new Date(tdEntry.registration.created);
            const timestamp = dt.getTime();
            // There's no need to keep track of the entire TD
            // so let's store only relevant information about it
            const obj = {
                "id":tdEntry.id,
                "timestamp":timestamp,
                "created":tdEntry.registration.created
            };
            // Check whether the listed TD should be deleted or consumed
            // and keep track of them in seperate dictionaries.
            // Only listed TDs with a timestamp newer than the time when the WoT-Bridge was started
            // will be considered as relevant. All others will be deleted.
            if (timestamp <= wotBridgeTimeStarted) {
                if (!wotToBeDeletedThingDict.has(obj.id)) {
                    wotToBeDeletedThingDict.set(obj.id, obj);
                }
            } else if (timestamp > wotBridgeTimeStarted) {
                if (!wotToBeConsumedThingDict.has(obj.id) && !wotIsConsumedThingDict.has(obj.id)) {
                    wotToBeConsumedThingDict.set(obj.id, obj);
                }
            }
        }
        // Print the results of TDs to be deleted
        console.log("Number of TDs to be deleted: " + wotToBeDeletedThingDict.size);
        if (wotToBeDeletedThingDict.size != 0) {
            for (const [key, value] of wotToBeDeletedThingDict.entries()) {
                console.log("TD to be deleted: " + JSON.stringify(key));
                // console.log(JSON.stringify(key) + ": " + JSON.stringify(value));
            }    
        }
        // Print the results of TDs to be consumed
        console.log("Number of TDs to be consumed: " + wotToBeConsumedThingDict.size);
        if (wotToBeConsumedThingDict.size != 0) {
            for (const [key, value] of wotToBeConsumedThingDict.entries()) {
                console.log("TD to be consumed: " + JSON.stringify(key));
                // console.log(JSON.stringify(key) + ": " + JSON.stringify(value));
            }    
        }
    }
}

// This function is to delete outdated TDs from the WoT-Hive dictionary
async function deleteTD(id) {
    try {
        // Delete all outdated TDs in the WoT-Hive directory
        const response = await fetch(URI_API_THINGS + "/" + id, {method: 'DELETE'});
        // Check if response status is ok (200 < status < 300)
        if (!response.ok) {
            const message = `An error has occured while deleting: ${response.status}`;
            throw new Error(message); 
        }
        return response.ok
    } catch (error) {
        console.error(error);
    }
}

// This function cleans up the WoT-Hive directory and deletets oudtated TDs
async function cleanUpWotHive() {
    if (wotToBeDeletedThingDict.size != 0 && wotHiveLocalTdCache.length != 0) {
        for (const key of wotToBeDeletedThingDict.keys()) {
            let successful = await deleteTD(key);
            if (successful) {
                console.log("TD entry has been deleted: " + key);
                wotToBeDeletedThingDict.delete(key);
            }
        }    
    }
}

async function createConsumedThings() {
    if (wotToBeConsumedThingDict.size != 0) {
        for (const [key, td ] of wotToBeConsumedThingDict.entries()) {

            let isRunning = false;
            let wotDevice;
            if (!wotIsConsumedThingDict.has(key)) {
                //  console.log("Dict size of to be consumed: " + wotIsConsumedThingDict.size);
                //Creating the instances of the binding servers
                let serverPort = parseInt(FIRST_HTTP_SERVER_PORT) + httpServerPortCount++;
                let httpServer = new HttpServer({port: serverPort});
                // //Build the servient object
                let servient = new Servient();
                servient.addClientFactory(new HttpClientFactory(null));
                //Add different bindings to the server
                servient.addServer(httpServer);
                servient.start().then((WoT) => {
                    wotDevice = new WotDevice(WoT, key);
                    wotDevice.startDevice();
                });
                console.log("New consumed thing with ID " + JSON.stringify(key) 
                        + " is now up and running on port " + serverPort);
                // Update dictionaries
                wotIsConsumedThingDict.set(key, td);
                wotToBeConsumedThingDict.delete(key);
            }
        }
        // Print the results of is consumed TDs
        console.log("Number of consumed TDs: " + wotIsConsumedThingDict.size);
        if (wotIsConsumedThingDict.size != 0) {
            for (const [key, value] of wotIsConsumedThingDict.entries()) {
                console.log("TD is now listed as consumed: " + JSON.stringify(key));
            }    
        }
    }
}

// Import the WoT consumed device, which in our case will be a smart-vehicle
WotDevice = require("./dist/consumed_vehicle_base.js").WotConsumedDevice
const { HttpClientFactory } = require('@node-wot/binding-http');
// Import the WoT core servient
Servient = require("@node-wot/core").Servient
// Import the required bindings
HttpServer = require("@node-wot/binding-http").HttpServer

// Print out the time when the WoT-bridge was started
console.log("Wot-bridge started: " + new Date(wotBridgeTimeStarted) + " ; UTC timestamp: " + wotBridgeTimeStarted);
setInterval(() => {
    // Utilising a so-called IIFE (Immediately Invoked Function Express) for top-level aysnc/await
    ( async () => {
        wotHiveLocalTdCache = await fetchWotHiveTdCache();
        checkCacheForRelevantEntries(wotHiveLocalTdCache);
        cleanUpWotHive();
        createConsumedThings();
    })();
}, 10000);

// -----------------------------------------------------------------------------


// // WotDevice = require("./dist/base.js").WotDevice
// WotDevice = require("./dist/consumed_vehicle_base.js").WotConsumedDevice
// const { HttpClientFactory } = require('@node-wot/binding-http');
// /*
// This project supports the registration of the generated TD to a TD directory
// Fill in the directory URI where the HTTP POST request to send the TD will be made
// If you leave it empty, registration thread will never execute, otherwise it will try to register every 10 seconds 
// */
// const TD_DIRECTORY = "http://localhost:9000/api/events/create?diff=true"


// Servient = require("@node-wot/core").Servient
// //Importing the required bindings
// HttpServer = require("@node-wot/binding-http").HttpServer

// //Creating the instances of the binding servers
// var httpServer = new HttpServer({port: 8090});

// //Building the servient object
// var servient = new Servient();
// servient.addClientFactory(new HttpClientFactory(null));
// //Adding different bindings to the server
// servient.addServer(httpServer);

// // const deviceId = "urn:uuid:13b5122b-ac41-452f-a72b-58b969e6a8cc";
// const testingURL = "http://localhost:8080/smart-vehicle";

// servient.start().then((WoT) => {
//     wotDevice = new WotDevice(WoT, testingURL); // TODO change the wotDevice to something that makes more sense
//     wotDevice.startDevice();
// });

// const dirUri = "http://localhost:9000/api/events?diff=false"; 
// var EventSource = require("eventsource");
// const sseDirectory = new EventSource(dirUri);

// var doInitialise = true;

// if (doInitialise) {
//     console.log("Adding event listener...");
//     sseDirectory.addEventListener('create', function(e) {
//         console.log("Event: 'create', data: " + e.data);
//       });
//     doInitialise = false;
// }

// while (true) {

//     sseDirectory.onopen = function(e) {
//         console.log("Event open");
//     }

//     sseDirectory.onerror = function(e) {
//         console.log("Event error");
//         if (this.readyState == sseDirectory.CONNECTING) {
//             console.log(`Reconnecting (readyState=${this.readyState})...`);
//         } else {
//             console.log("An error has occured!");
//         }
//     }

//     sseDirectory.onmessage = function(e) {
//         console.log("Event onMessage received");
//         const { t } = JSON.parse(e.data);
//         console.log(t);
//         doInitialise = true;

//     }

//     sseSource.addEventListener('create', function (e) {
//         console.log("OnMessage...")
//         const { t } = JSON.parse(e.data);
//         console.log(t);
//         printWaitMessage = true;
       
//     });
// }

