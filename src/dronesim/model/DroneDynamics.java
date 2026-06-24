package dronesim.model;

/**
 * Represents time-varying dynamic data for a single drone at a specific point in time.
 */
public class DroneDynamics {
    private String droneUrl;
    private int droneId;
    private String timestamp;
    private int speed;
    private double alignRoll;
    private double alignPitch;
    private double alignYaw;
    private double longitude;
    private double latitude;
    private int batteryStatus;
    private String lastSeen;
    private String status;

    public DroneDynamics(String droneUrl, int droneId, String timestamp, int speed,
                         double alignRoll, double alignPitch, double alignYaw,
                         double longitude, double latitude, int batteryStatus,
                         String lastSeen, String status) {
        this.droneUrl = droneUrl;
        this.droneId = droneId;
        this.timestamp = timestamp;
        this.speed = speed;
        this.alignRoll = alignRoll;
        this.alignPitch = alignPitch;
        this.alignYaw = alignYaw;
        this.longitude = longitude;
        this.latitude = latitude;
        this.batteryStatus = batteryStatus;
        this.lastSeen = lastSeen;
        this.status = status;
    }

    public String getDroneUrl() { return droneUrl; }
    public int getDroneId() { return droneId; }
    public String getTimestamp() { return timestamp; }
    public int getSpeed() { return speed; }
    public double getAlignRoll() { return alignRoll; }
    public double getAlignPitch() { return alignPitch; }
    public double getAlignYaw() { return alignYaw; }
    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }
    public int getBatteryStatus() { return batteryStatus; }
    public String getLastSeen() { return lastSeen; }
    public String getStatus() { return status; }
}
