package lf.core;

/**
 * Toy class to represent a range of vehicles owned by a FleetManager
 * In a real system this would be some set of values (perhaps a hashmap)
 * persisted in a db...
 */
public class VehicleIdRange
{
    private long low;
    private long high;

    public VehicleIdRange(long low, long high){
        this.low = low;
        this.high = high;
    }

    public boolean contains(long number){
        return (number >= low && number <= high);
    }
}