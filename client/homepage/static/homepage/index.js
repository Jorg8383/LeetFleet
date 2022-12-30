// call to get cars
// let fleets = [{"managerId":"10000","name":"Fastidious"},{"managerId":"10001","name":"Careless"},{"managerId":"10002","name":"Paranoid"},{"managerId":"10003","name":"Fleetless"}]

let current_vehicle_for_lock;
async function getVehicles(id) {
  var url = "http://localhost:8080/web/list_vehicles?fleetManager=" + id;

  console.log(url);
  $.support.cors = true;

  $.ajax({
    url: url,
    type: "POST",

    dataType: "json",

    headers: {
      "Access-Control-Allow-Origin": "*",
    },
    success: function (data) {
      showFleetVehicles(data);
    },
    error: function (jqXhr, textStatus, errorMessage) {
      console.log("error message: " + textStatus + jqXhr);
    },
  });
}

async function editVehicle(vehicleJson) {
  var url = "http://localhost:8080/web";
  console.log(
    "vehicle json just before transmit: " + JSON.stringify(vehicleJson)
  );
  $.ajax({
    url: url,
    type: "POST",
    data: JSON.stringify(vehicleJson),
    contentType: "application/json; charset=utf-8",
    headers: {
      "Access-Control-Allow-Origin": "*",
    },
    success: function (newJson) {
      console.log("Vehicle change success!");
      console.log("The json after change is: " + JSON.stringify(newJson));
    },
    error: function (jqXhr, textStatus, errorMessage) {
      console.log("error message: " + textStatus + jqXhr);
    },
  });
}

async function getFleets() {
  var url = "http://localhost:8080/web/list_fleets";
  $.support.cors = true;

  $.ajax({
    url: "http://localhost:8080/web/list_fleets",
    type: "GET",

    dataType: "json",

    headers: {
      "Access-Control-Allow-Origin": "*",
    },
    success: function (data) {
      setUpDropDown(data);
    },
    error: function (jqXhr, textStatus, errorMessage) {
      console.log("error message: " + textStatus + jqXhr);
    },
  });
}

var home = document.querySelector(".home");

getFleets();

// dummy json

let dummy_json = [
  {
    vehicleId: "WoT-ID-Mfr-VIN-1",
    fleetManager: "fleetless",
    tdURL: "http://localhost:8080/smart-vehicle/",
    oilLevel: 50,
    tyrePressure: 30,
    mileage: 10000,
    nextServiceDistance: 10000,
    doorStatus: "LOCKED",
    maintenanceNeeded: false,
  },

  {
    vehicleId: "WoT-ID-Mfr-VIN-2",
    fleetManager: "fleetless",
    tdURL: "http://localhost:8080/smart-vehicle/",
    oilLevel: 50,
    tyrePressure: 30,
    mileage: 10000,
    nextServiceDistance: 10000,
    doorStatus: "UNLOCKED",
    maintenanceNeeded: false,
  },

  {
    vehicleId: "WoT-ID-Mfr-VIN-3",
    fleetManager: "fleetless",
    tdURL: "http://localhost:8080/smart-vehicle/",
    oilLevel: 50,
    tyrePressure: 30,
    mileage: 10000,
    nextServiceDistance: 10000,
    doorStatus: "LOCKED",
    maintenanceNeeded: false,
  },
  {
    vehicleId: "WoT-ID-Mfr-VIN-1",
    fleetManager: "fleetless",
    tdURL: "http://localhost:8080/smart-vehicle/",
    oilLevel: 50,
    tyrePressure: 30,
    mileage: 10000,
    nextServiceDistance: 10000,
    doorStatus: "LOCKED",
    maintenanceNeeded: false,
  },

  {
    vehicleId: "WoT-ID-Mfr-VIN-2",
    fleetManager: "fleetless",
    tdURL: "http://localhost:8080/smart-vehicle/",
    oilLevel: 50,
    tyrePressure: 30,
    mileage: 10000,
    nextServiceDistance: 10000,
    doorStatus: "UNLOCKED",
    maintenanceNeeded: false,
  },

  {
    vehicleId: "WoT-ID-Mfr-VIN-3",
    fleetManager: "fleetless",
    tdURL: "http://localhost:8080/smart-vehicle/",
    oilLevel: 50,
    tyrePressure: 30,
    mileage: 10000,
    nextServiceDistance: 10000,
    doorStatus: "LOCKED",
    maintenanceNeeded: false,
  },
];

function showVehicle(vehicleJson) {
  var newInnerHtml = "";
  newInnerHtml +=
    "<div class='vehicle-div' id='div-" + vehicleJson.vehicleId + "'>";
  // newInnerHtml += "<h1>" + vehicleJson.fleetManager + "</h1>"
  newInnerHtml += "<p> Vehicle ID: " + vehicleJson.vehicleId + "</p>";
  newInnerHtml += "<p> oilLevel: " + vehicleJson.oilLevel + "</p>";
  newInnerHtml += "<p> tyrePressure " + vehicleJson.tyrePressure + "</p>";
  newInnerHtml += "<p> mileage: " + vehicleJson.mileage + "</p>";
  newInnerHtml +=
    "<p> nextServiceDistance: " + vehicleJson.nextServiceDistance + "</p>";
  newInnerHtml +=
    "<p class='ds-" +
    vehicleJson.vehicleId +
    "'>doorStatus: " +
    vehicleJson.doorStatus +
    "</p>";
  newInnerHtml +=
    "<p> Maintanence needed: " + vehicleJson.maintenanceNeeded + "</p>";
  newInnerHtml +=
    "<button class='btn btn-warning vehicle-btn' value='" +
    vehicleJson.vehicleId +
    "'>" +
    vehicleJson.vehicleId +
    "</button>";

  newInnerHtml += "</div>";

  return newInnerHtml;
}

function setUpDropDown(fleetMgrs) {
  let insideDropdown = "";
  fleetMgrs.forEach((element) => {
    insideDropdown +=
      "<a class='dropdown-item' id='" +
      element.managerId +
      "'>" +
      element.name +
      " Manager</a>";

    document.getElementsByClassName("dropdown-menu")[0].innerHTML =
      insideDropdown;
  });
  let dropdownItems = document.querySelectorAll(".dropdown-item");

  dropdownItems.forEach((element) => {
    element.addEventListener("click", () => {
      getVehicles(element.id);
    });
  });
}

function showFleetVehicles(fleet) {
  let vehicleDiv = document.getElementById("vehicles");
  // make api call to get vehicles from akka

  let InnerHtml = "";
  console.log(fleet);
  //change dummy json to fleet
  fleet.forEach((vehicleJson) => {
    InnerHtml += showVehicle(vehicleJson);
  });

  vehicleDiv.style.display = "grid";
  vehicleDiv.style.gridTemplateColumns = "30% 30% 30%";
  vehicleDiv.style.justifyContent = "center";

  home.style.display = "none";
  vehicleDiv.innerHTML = InnerHtml;
  addEventListenertoButtons(fleet);

  document.querySelector(".back").classList.remove("back-hidden");
  document.querySelector(".back").classList.add("back-shown");
}

document.querySelector(".back").addEventListener("click", () => {
  let vehicleDiv = document.getElementById("vehicles");
  vehicleDiv.style.display = "none";
  home.style.display = "inline-block";

  document.querySelector(".back").classList.add("back-hidden");
  document.querySelector(".back").classList.remove("back-shown");
});

let dropdownItems = document.querySelectorAll(".dropdown-item");

function addEventListenertoButtons(fleet) {
  let btn = document.querySelectorAll(".vehicle-btn");

  btn.forEach((button) => {
    button.addEventListener("click", () => {
      let individualDiv = document.querySelector("#div-" + button.innerHTML);
      individualDiv.style.display = "inline";
      let vehicleDiv = document.getElementById("vehicles");

      //dummy_json.forEach(json => {
      fleet.forEach((json) => {
        if (json.vehicleId === button.innerHTML) {
          activeJson = json;
        }
      });
      console.log("Current json = " + activeJson);

      let allVDivs = document.querySelectorAll(".vehicle-div");

      allVDivs.forEach((div) => {
        div.style.display = "none";
      });
      individualDiv.style.display = "inline-block";

      let doorStatusCompliment = "";

      let doorStatusElement = document.querySelectorAll(
        ".ds-" + button.innerHTML
      )[0];
      if (doorStatusElement.innerHTML === "doorStatus: LOCKED") {
        doorStatusCompliment = "UNLOCK";
      } else if (doorStatusElement.innerHTML === "doorStatus: UNLOCKED") {
        doorStatusCompliment = "LOCK";
      } else {
        console.log(doorStatusElement.innerHTML);
      }

      individualDiv.innerHTML +=
        "<button class='btn btn-primary lock-btn' id='lBtn-" +
        button.innerHTML +
        "'>" +
        doorStatusCompliment +
        "</button>";
      var lockBtn = document.querySelector("#lBtn-" + button.innerHTML);
      lockBtn.addEventListener("click", () => {
        var specificLockBtn = document.querySelector(
          "#lBtn-" + button.innerHTML
        );
        let doorElement = document.querySelectorAll(
          ".ds-" + button.innerHTML
        )[0];
        if (doorStatusCompliment === "LOCK") {
          // Action was 'Lock' the doors
          activeJson.doorStatus = "LOCKED";
          // console.log("In the LOCK block " + doorStatusElement.innerHTML );
          doorElement.innerHTML = "doorStatus: LOCKED";
          // New action is 'Unlock' the doors.
          doorStatusCompliment = "UNLOCK";
        } else if (doorStatusCompliment === "UNLOCK") {
          // Action was 'Unlock' the doors
          activeJson.doorStatus = "UNLOCKED";
          // console.log("In the UNLOCK block " + doorStatusElement.innerHTML );
          doorElement.innerHTML = "doorStatus: UNLOCKED";
          // New action is 'Unlock' the doors.
          doorStatusCompliment = "LOCK";
        }
        specificLockBtn.innerHTML = doorStatusCompliment;
        editVehicle(activeJson);
      });
    });
  });
}
