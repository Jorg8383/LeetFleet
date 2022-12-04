package lf.model;

public class Vehicle {

    enum LockStatuses {
        LOCKED,
        UNLOCKED
    }

    private String vehicleId;  // WoT VehicleId (String, e.g. "WoT-ID-Mfr-VIN-nnnn")
    private long vehicleIdLong;  // LeetFleet VehicleId (long i.e. just an 'id number', e.g. nnnn)

    private String fleetManager;

    // The unique URL to access the exposed thing for this vehicle : "http://localhost:1234/WoT-ID-Mfr-VIN-1234"
    private String tdURL;

    private Float tyrePressure;
    private Float mileage;
    private Float oilLevel;
    private Long nextServiceDistance;
    private String doorStatus;
    private String maintenanceNeeded;

    // Constructors:
    public Vehicle(
            String vehicleId, String fleetManager, String tdURL,
            Float tyrePressure, Float mileage, Float oilLevel,
            Long nextServiceDistance, String doorStatus, String maintenanceNeeded)
    {
        this.vehicleId = vehicleId;
        this.vehicleIdLong = wotIdToLongId(vehicleId);  // Convert formatted WoT Id to Long
        //
        this.fleetManager = fleetManager;
        //
        this.tdURL = tdURL;
        //
        this.tyrePressure = tyrePressure;
        this.mileage = mileage;
        this.oilLevel = oilLevel;
        this.nextServiceDistance = nextServiceDistance;
        this.doorStatus = doorStatus;
        this.maintenanceNeeded = maintenanceNeeded;
    }

    public Vehicle() {
    }

    public static Vehicle createForMileage(String vehicleId, String fleetId, Float mileage) {
        return new Vehicle(
                vehicleId, fleetId, "",
                new Float(0), mileage, new Float(0),
                new Long(0), "", "");
    }

    public static Vehicle createTemplate(String vehicleId) {
        return new Vehicle(
                vehicleId, "", "",
                new Float(0), new Float(0), new Float(0),
                new Long(0), "", "");
    }

    public Vehicle(String vehicleId, String fleetId) {
        this.vehicleId = vehicleId;
        this.fleetManager = fleetId;
        // this.isOn = isOn;
    }
    //------------------------------------------------------------

    /**
     *
     * @param wotVehicleId
     * @return
     */
    public static long wotIdToLongId(String wotVehicleId) {
        long vehicleIdLong = 0;
        try {
            vehicleIdLong =
                Long.parseLong(wotVehicleId.substring(15));  // LeetFleet VehicleId (long, e.g. nnnn)
        } catch (Exception e) {
            // Not concerned with the nature of the exception - just that one occurred
            // Toy system: not valid => ignore.
        }
        return vehicleIdLong;
    }

    //------------------------------------------------------------

    public String getVehicleId() {
        return this.vehicleId;
    }
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;    // WoT VehicleId (String, e.g. "WoT-ID-Mfr-VIN-nnnn")
        this.vehicleIdLong = wotIdToLongId(vehicleId);
    }

    public long getVehicleIdLong() {
        return this.vehicleIdLong;
    }
    // Will we ever set this directly??
    public void setVehicleIdLong(Long vehicleIdLong) {
        this.vehicleIdLong = vehicleIdLong;
    }

    //------------------------------------------------------------

    public String getFleetManager() {
        return this.fleetManager;
    }
    public void setFleetManager(String fleetManager) {
        this.fleetManager = fleetManager;
    }

    //------------------------------------------------------------

    public String getTdURL() {
        return tdURL;
    }
    public void setTdURL(String tdURL) {
        this.tdURL = tdURL;
    }

    //------------------------------------------------------------

    public Float getTyrePressure() {
        return tyrePressure;
    }
    public void setTyrePressure(Float tyrePressure) {
        this.tyrePressure = tyrePressure;
    }

    public Float getMileage() {
        return mileage;
    }
    public void setMileage(Float mileage) {
        this.mileage = mileage;
    }

    public Float getOilLevel() {
        return oilLevel;
    }
    public void setOilLevel(Float oilLevel) {
        this.oilLevel = oilLevel;
    }

    public Long getNextServiceDistance() {
        return nextServiceDistance;
    }
    public void setNextServiceDistance(Long nextServiceDistance) {
        this.nextServiceDistance = nextServiceDistance;
    }

    public String getDoorStatus() {
        return doorStatus;
    }
    public void setDoorStatus(String doorStatus) {
        this.doorStatus = doorStatus;
    }

    public String getMaintenanceNeeded() {
        return maintenanceNeeded;
    }
    public void setMaintenanceNeeded(String maintenanceNeeded) {
        this.maintenanceNeeded = maintenanceNeeded;
    }

}