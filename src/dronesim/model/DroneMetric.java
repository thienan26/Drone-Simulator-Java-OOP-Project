package dronesim.model;

/**
 * Four calculated metrics per drone:
 * 1. batteryAlert    – "SẮP HẾT PIN" (&lt;20%), "TRUNG BÌNH" (&lt;50%), "TỐT" (≥50%)
 * 2. averageSpeed    – average km/h of the most recent dynamics records
 * 3. connectionState – "ONLINE" (last-seen ≤5 min), "STALE", "NO DATA"
 * 4. isActive        – true when connectionState == ONLINE (used for fleet active count)
 */
public class DroneMetric {

    private final int    droneId;
    private final double batteryPercent;
    private final String batteryAlert;
    private final double averageSpeed;
    private final double currentSpeed;
    private final String connectionState;
    private final boolean isActive;
    private final String  status;

    public DroneMetric(int droneId, double batteryPercent, String batteryAlert,
                       double averageSpeed, double currentSpeed,
                       String connectionState, boolean isActive, String status) {
        this.droneId         = droneId;
        this.batteryPercent  = batteryPercent;
        this.batteryAlert    = batteryAlert;
        this.averageSpeed    = averageSpeed;
        this.currentSpeed    = currentSpeed;
        this.connectionState = connectionState;
        this.isActive        = isActive;
        this.status          = status;
    }

    public int     getDroneId()         { return droneId; }
    public double  getBatteryPercent()  { return batteryPercent; }
    public String  getBatteryAlert()    { return batteryAlert; }
    public double  getAverageSpeed()    { return averageSpeed; }
    public double  getCurrentSpeed()    { return currentSpeed; }
    public String  getConnectionState() { return connectionState; }
    public boolean isActive()           { return isActive; }
    public String  getStatus()          { return status; }
}
