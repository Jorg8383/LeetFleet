let current_vehicle_for_lock;
async function getVehicles(id, manager) {
  var url = "http://localhost:8080/web/list_vehicles?fleetManager=" + id;

  $.support.cors = true;

  $.ajax({
    url: url,
    type: "POST",

    dataType: "json",

    headers: {
      "Access-Control-Allow-Origin": "*",
    },
    success: function (data) {
      showFleetVehicles(data, manager);
    },
    error: function (jqXhr, textStatus, errorMessage) {
      console.log("error message: " + textStatus);
    },
  });
}

async function editVehicle(vehicleJson) {
  var url = "http://localhost:8080/web";

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
      console.log("error message: " + textStatus);
    },
  });
}

var home = document.querySelector(".home");

getFleets();

function showVehicle(vehicleJson, fleetManager) {
  var newInnerHtml = "";
  newInnerHtml +=
    "<div class='vehicle-div' id='div-" + vehicleJson.vehicleId + "'>";
  newInnerHtml += "<h1>" + fleetManager + "</h1>";
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
      getVehicles(element.id, element.innerHTML);
    });
  });
}

function showFleetVehicles(fleet, manager) {
  let vehicleDiv = document.getElementById("vehicles");
  // make api call to get vehicles from akka

  let InnerHtml = "";

  fleet.forEach((vehicleJson) => {
    InnerHtml += showVehicle(vehicleJson, manager);
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

document.querySelector(".title").addEventListener("click", () => {
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

      fleet.forEach((json) => {
        if (json.vehicleId === button.innerHTML) {
          activeJson = json;
        }
      });

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
        console.log("ERROR: Incompatable door status");
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

          doorElement.innerHTML = "doorStatus: LOCKED";
          // New action is 'Unlock' the doors.
          doorStatusCompliment = "UNLOCK";
        } else if (doorStatusCompliment === "UNLOCK") {
          // Action was 'Unlock' the doors
          activeJson.doorStatus = "UNLOCKED";

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
