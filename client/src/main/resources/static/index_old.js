
// call to get cars
// let fleets = [{"managerId":"10000","name":"Fastidious"},{"managerId":"10001","name":"Careless"},{"managerId":"10002","name":"Paranoid"},{"managerId":"10003","name":"Fleetless"}]

async function getFleets(){
    console.log("gothere")
    var xhttp = new XMLHttpRequest();
    xhttp.onload = function() {
            var response = xhttp.response
           setUpDropDown(JSON.parse(response))
        }
    xhttp.open("GET", "http://localhost:8080/web/list_fleets", true);
    xhttp.send();
   
    

}

var home = document.querySelector(".home")

getFleets()

// dummy json

let dummy_json = [

{ "vehicleId" : "WoT-ID-Mfr-VIN-1",
"fleetManager" : "fleetless",
"tdURL" : "http://localhost:8080/smart-vehicle",
"oilLevel" : 50,
"tyrePressure" : 30,
"mileage" : 10000,
"nextServiceDistance" : 10000,
"doorStatus" : "LOCKED",
"maintenanceNeeded" : false
},

{ "vehicleId" : "WoT-ID-Mfr-VIN-2",
"fleetManager" : "fleetless",
"tdURL" : "http://localhost:8080/smart-vehicle",
"oilLevel" : 50,
"tyrePressure" : 30,
"mileage" : 10000,
"nextServiceDistance" : 10000,
"doorStatus" : "UNLOCKED",
"maintenanceNeeded" : false
},

{ "vehicleId" : "WoT-ID-Mfr-VIN-3",
"fleetManager" : "fleetless",
"tdURL" : "http://localhost:8080/smart-vehicle",
"oilLevel" : 50,
"tyrePressure" : 30,
"mileage" : 10000,
"nextServiceDistance" : 10000,
"doorStatus" : "LOCKED",
"maintenanceNeeded" : false
},

]


//curreent vehicle
var activeJson = {};

function showVehicle(vehicleJson){
        var newInnerHtml = ""
        newInnerHtml += "<div class='vehicle-div' id='div-"+ vehicleJson.vehicleId +"'>"
        newInnerHtml += "<h1>" + vehicleJson.fleetManager + "</h1>"
        newInnerHtml += "<p> Vehicle ID: " + vehicleJson.vehicleId + "</p>";
        newInnerHtml += "<p> oilLevel: " + vehicleJson.oilLevel + "</p>";
        newInnerHtml += "<p> tyrePressure " + vehicleJson.tyrePressure + "</p>";
        newInnerHtml += "<p> mileage: " + vehicleJson.mileage + "</p>";
        newInnerHtml += "<p> nextServiceDistance: " + vehicleJson.nextServiceDistance + "</p>";
        newInnerHtml += "<p class='ds-"+ vehicleJson.vehicleId +"'>doorStatus: " + vehicleJson.doorStatus + "</p>";
        newInnerHtml += "<p> Maintanence needed: " + vehicleJson.maintenanceNeeded + "</p>";
        newInnerHtml += "<button class='btn btn-warning vehicle-btn' value='" + vehicleJson.vehicleId + "'>" + vehicleJson.vehicleId + "</button>";

        newInnerHtml += "<p>-------------------------------------------------------------------</p>";
        newInnerHtml += "</div>"

        return newInnerHtml

}

function setUpDropDown(fleetMgrs) {
        let insideDropdown = "";
        fleetMgrs.forEach(element => {
        insideDropdown += "<a class='dropdown-item' value='" + element.name + "'href='#'>" + element.name +" Manager</a>";
        

        document.getElementsByClassName("dropdown-menu")[0].innerHTML = insideDropdown;
        })
        let dropdownItems = document.querySelectorAll(".dropdown-item")

        dropdownItems.forEach(element => {
            element.addEventListener("click", () => {
                // console.log(element.classList)
                var indiVehicle = showFleetVehicles(element.innerHTML);

            })
        })
};


// let insideDropdown = "";
// fleets.forEach(element => {
//     insideDropdown += "<a class='dropdown-item' value='" + element.name + "'href='#'>" + element.name +" Manager</a>";
// });



function showFleetVehicles(fleet){
    let vehicleDiv = document.getElementById("vehicles")
    // make api call to get vehicles from akka
    
    let InnerHtml = "";
    dummy_json.forEach(vehicleJson => {

        InnerHtml += showVehicle(vehicleJson)
    })

    vehicleDiv.style.display = "inline-block";
    home.style.display = "none";
    vehicleDiv.innerHTML = InnerHtml;
    addEventListenertoButtons();

    document.querySelector(".back").classList.remove("back-hidden")
    document.querySelector(".back").classList.add("back-shown")

}

document.querySelector(".back").addEventListener("click", () => {
    let vehicleDiv = document.getElementById("vehicles")
    vehicleDiv.style.display = "none";
    home.style.display = "inline-block";
    
    document.querySelector(".back").classList.add("back-hidden")
    document.querySelector(".back").classList.remove("back-shown")
})

let dropdownItems = document.querySelectorAll(".dropdown-item")

dropdownItems.forEach(element => {
    element.addEventListener("click", () => {
        // console.log(element.classList)
        var indiVehicle = showFleetVehicles(element.innerHTML);

    })
})


function addEventListenertoButtons(){
    let btn = document.querySelectorAll(".vehicle-btn");

    btn.forEach(button => {
        button.addEventListener("click", () => {
            let individualDiv = document.querySelector("#div-" + button.innerHTML);
            individualDiv.style.display = "inline";
            let vehicleDiv = document.getElementById("vehicles")
            
            dummy_json.forEach(json => {
                if (json.vehicleId === button.innerHTML){
                    activeJson = json;
                }
            })
            console.log("Current json = " + activeJson);
            
            let allVDivs = document.querySelectorAll(".vehicle-div")

            allVDivs.forEach(div => {
                div.style.display = "none";
            })
            individualDiv.style.display = "inline-block";

            let doorStatusCompliment = "";

            let doorStatusElement = document.querySelectorAll(".ds-" + button.innerHTML)[0]
            if (doorStatusElement.innerHTML === "doorStatus: LOCKED"){
                doorStatusCompliment = "UNLOCK"
            } else if (doorStatusElement.innerHTML === "doorStatus: UNLOCKED"){
                doorStatusCompliment = "LOCK"
            }
            else{
                console.log(doorStatusElement.innerHTML)
            }


            individualDiv.innerHTML += "<button class='btn btn-primary'>" + doorStatusCompliment +"</button>"
            

        })
    })
}

