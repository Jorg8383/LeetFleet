const URI_API_THINGS = "http://localhost:9000/api/things";

const wotBridgeTimeStarted = Date.now();
let wotHiveLocalTdCache = {};
let wotIsConsumedThingDict = new Map();
let wotToBeConsumedThingDict = new Map();
let wotToBeDeletedThingDict = new Map();
let consumedIdCount = 1;

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

// This function to find all relevant (newly created) and all outdated TD entries.
// It stores the "id" of all relevant and outdated TDs from the WoT-Hive dictionary
// in the dictionaries "wotToBeConsumedThingDict" and "wotToBeDeletedThingDict" respectively.
function checkCacheForRelevantEntries(cache) {
    console.log("WoT-Hive cache contains: " + cache.length + " TD entries.");
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
            }
            // Check whether the listed TD should be deleted or consumed
            // and keep track of them in seperate dictionaries
            if (timestamp <= wotBridgeTimeStarted) {
                if (!wotToBeDeletedThingDict.has(obj.id)) {
                    wotToBeDeletedThingDict.set(deleteIdKey++, obj);
                }
            } else if (timestamp > wotBridgeTimeStarted) {
                if (!wotToBeConsumedThingDict.has(obj.id)) {
                    wotToBeConsumedThingDict.set(obj.id, obj);
                }
            }
        }
        // Print the results of TDs to be deleted
        console.log("Number of TDs to be deleted: " + wotToBeDeletedThingDict.size);
        if (wotToBeDeletedThingDict.size != 0) {
            for (const [key, value] of wotToBeDeletedThingDict.entries()) {
                console.log(JSON.stringify(key) + ": " + JSON.stringify(value));
            }    
        }
        // Print the results of TDs to be consumed
        console.log("Number of TDs to be consumed: " + wotToBeConsumedThingDict.size);
        if (wotToBeConsumedThingDict.size != 0) {
            for (const [key, value] of wotToBeConsumedThingDict.entries()) {
                console.log(JSON.stringify(key) + ": " + JSON.stringify(value));
            }    
        }
    } else {
        console.error("WoT-Hive cache is empty!");
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

async function cleanUpWotHive() {
    if (wotToBeDeletedThingDict.size != 0 && wotHiveLocalTdCache.length != 0) {
        for (const [key, td] of wotToBeDeletedThingDict.entries()) {
            let successful = await deleteTD(td.id);
            if (successful) {
                console.log("TD entry deleted: " + td.id);
            }
        }    
    }
    wotToBeDeletedThingDict = new Map();
}

async function consumeRelevantTDs() {
    ;
}


// Print out the time when the WoT-bridge was started
console.log("Wot-bridge started: " + new Date(wotBridgeTimeStarted) + " ; UTC timestamp: " + wotBridgeTimeStarted);

setInterval(() => {
    // Utilising a so-called IIFE (Immediately Invoked Function Express) for top-level aysnc/await
    ( async () => {
        wotHiveLocalTdCache = await fetchWotHiveTdCache();
        checkCacheForRelevantEntries(wotHiveLocalTdCache);
        cleanUpWotHive();
        
    })();
}, 10000);


// const fetchWotHiveTdCache = async () => {
//     try {
//         const URI_WOT_HIVE_TD_LIST = "http://localhost:9000/api/things";
//         const response = await fetch(URI_WOT_HIVE_TD_LIST);
//         if (!response.ok) {
//             const message = `An error has occured: ${response.status}`;
//             throw new Error(message); 
//         }
//         const tdCache = await response.json();
//         return tdCache;
//     } catch (error) {
//         console.error(error);
//     }
// }

// wotHiveLocalTdCache = await fetchWotHiveTdCache();
// fetchWotHiveTdCache().then((data) => console.log(data));
// fetchWotHiveTdCache().then((data) => wotHiveLocalTdCache = data);
// console.log(wotHiveLocalTdCache);



// async function updateLocalTdCache() {
//     wotHiveLocalTdCache = await fetchTdList(URI_WOT_HIVE_TD_LIST);
//     console.log(wotHiveLocalTdCache);
    
// }

// async function fetchTdList(uri) {
//     try {
//         let response = await fetch(uri);
//         let data = await response.json();
//         return data;    
//     } catch (error) {
//             console.error(error);
//         }
// }

// function checkCacheForNewEntries() {
//     console.log("WoT-Hive cache size: " + wotHiveLocalTdCache.length);
//     if (wotHiveLocalTdCache.length != 0) {
//         for (let i=0; i < wotHiveLocalTdCache.length; i++) {
//             let tdEntry = wotHiveLocalTdCache[i];
//             console.log(tdEntry.id);
//         }
//     } else {
//         console.log("WoT-Hive cache is empty!");
//     }
// }

// updateLocalTdCache();
// checkCacheForNewEntries();
// -----------------------------------------------------------------------------

// //Where your concrete implementation is included
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

// // servient.start().then((WoT) => {
// //     wotDevice = new WotDevice(WoT, testingURL); // TODO change the wotDevice to something that makes more sense
// //     wotDevice.startDevice();
// // });

// // const dirUri = "http://localhost:9000/api/events?diff=false"; 
// // var EventSource = require("eventsource");
// // const sseDirectory = new EventSource(dirUri);

// // var doInitialise = true;

// // if (doInitialise) {
// //     console.log("Adding event listener...");
// //     sseDirectory.addEventListener('create', function(e) {
// //         console.log("Event: 'create', data: " + e.data);
// //       });
// //     doInitialise = false;
// // }

// // while (true) {

// //     sseDirectory.onopen = function(e) {
// //         console.log("Event open");
// //     }

// //     sseDirectory.onerror = function(e) {
// //         console.log("Event error");
// //         if (this.readyState == sseDirectory.CONNECTING) {
// //             console.log(`Reconnecting (readyState=${this.readyState})...`);
// //         } else {
// //             console.log("An error has occured!");
// //         }
// //     }

// //     sseDirectory.onmessage = function(e) {
// //         console.log("Event onMessage received");
// //         const { t } = JSON.parse(e.data);
// //         console.log(t);
// //         doInitialise = true;

// //     }

// //     sseSource.addEventListener('create', function (e) {
// //         console.log("OnMessage...")
// //         const { t } = JSON.parse(e.data);
// //         console.log(t);
// //         printWaitMessage = true;
       
// //     });
// // }

