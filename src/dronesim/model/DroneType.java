package dronesim.model;

/**
 * Represents a drone model/type with static attributes shared by all drones of that model.
 */
public class DroneType {
    private int id;
    private String manufacturer;
    private String typeName;
    private int weight;
    private int maxSpeed;
    private int maxCarriageWeight;
    private int maxCarriage;
    private int batteryCapacity;
    private int controlRange;
    private int maxCarriageWeightKg;

    public DroneType(int id, String manufacturer, String typeName, int weight,
                     int maxSpeed, int maxCarriageWeight, int maxCarriage,
                     int batteryCapacity, int controlRange) {
        this.id = id;
        this.manufacturer = manufacturer;
        this.typeName = typeName;
        this.weight = weight;
        this.maxSpeed = maxSpeed;
        this.maxCarriageWeight = maxCarriageWeight;
        this.maxCarriage = maxCarriage;
        this.batteryCapacity = batteryCapacity;
        this.controlRange = controlRange;
    }

    public int getId() { return id; }
    public String getManufacturer() { return manufacturer; }
    public String getTypeName() { return typeName; }
    public int getWeight() { return weight; }
    public int getMaxSpeed() { return maxSpeed; }
    public int getMaxCarriageWeight() { return maxCarriageWeight; }
    public int getMaxCarriage() { return maxCarriage; }
    public int getBatteryCapacity() { return batteryCapacity; }
    public int getControlRange() { return controlRange; }

    @Override
    public String toString() {
        return manufacturer + " " + typeName + " (ID:" + id + ")";
    }
}
