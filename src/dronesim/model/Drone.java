package dronesim.model;

/**
 * Represents an individual drone with its unique attributes.
 */
public class Drone {
    private int id;
    private String droneTypeUrl;
    private int droneTypeId;
    private String serialNumber;
    private int carriageWeight;
    private String carriageType;

    public Drone(int id, String droneTypeUrl, int droneTypeId,
                 String serialNumber, int carriageWeight, String carriageType) {
        this.id = id;
        this.droneTypeUrl = droneTypeUrl;
        this.droneTypeId = droneTypeId;
        this.serialNumber = serialNumber;
        this.carriageWeight = carriageWeight;
        this.carriageType = carriageType;
    }

    public int getId() { return id; }
    public String getDroneTypeUrl() { return droneTypeUrl; }
    public int getDroneTypeId() { return droneTypeId; }
    public String getSerialNumber() { return serialNumber; }
    public int getCarriageWeight() { return carriageWeight; }
    public String getCarriageType() { return carriageType; }

    @Override
    public String toString() {
        return "Drone #" + id + " [" + serialNumber + "]";
    }
}
