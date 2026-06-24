package dronesim.service;

import dronesim.model.Drone;
import dronesim.model.DroneDynamics;
import dronesim.model.DroneMetric;
import dronesim.model.DroneType;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Calculates four metrics for each drone from its recent dynamics history.
 *
 * <ul>
 *   <li><b>Battery Alert</b>  – SẮP HẾT PIN (&lt;20%), TRUNG BÌNH (&lt;50%), TỐT (≥50%)</li>
 *   <li><b>Average Speed</b>  – mean km/h across the supplied recent dynamics records</li>
 *   <li><b>Connection State</b> – ONLINE (last-seen ≤5 min ago) / STALE / NO DATA</li>
 *   <li><b>Is Active</b>      – boolean; true when connection state is ONLINE</li>
 * </ul>
 */
public class MetricsService {

    /**
     * Computes all four metrics using the most recent dynamics records for a drone.
     *
     * @param drone          the individual drone
     * @param type           the drone's type/model (for battery capacity); may be null
     * @param recentDynamics list of recent dynamics, newest first; may be empty
     * @return fully populated {@link DroneMetric}
     */
    public DroneMetric calculate(Drone drone, DroneType type, List<DroneDynamics> recentDynamics) {
        DroneDynamics latest = recentDynamics.isEmpty() ? null : recentDynamics.get(0);

        double batteryPercent  = computeBatteryPercent(latest, type);
        String batteryAlert    = classifyBattery(batteryPercent);
        double averageSpeed    = computeAverageSpeed(recentDynamics);
        double currentSpeed    = latest != null ? latest.getSpeed() : 0;
        String connectionState = classifyConnection(latest);
        boolean isActive       = "ONLINE".equals(connectionState);
        String  status         = latest != null ? latest.getStatus() : "N/A";

        return new DroneMetric(
                drone.getId(),
                batteryPercent,
                batteryAlert,
                averageSpeed,
                currentSpeed,
                connectionState,
                isActive,
                status
        );
    }

    // ── metric 1 : battery alert ──────────────────────────────────────────────

    private double computeBatteryPercent(DroneDynamics d, DroneType t) {
        if (d == null || t == null || t.getBatteryCapacity() <= 0) return -1;
        return (d.getBatteryStatus() * 100.0) / t.getBatteryCapacity();
    }

    private String classifyBattery(double pct) {
        if (pct < 0)   return "KHÔNG RÕ";
        if (pct < 20)  return "SẮP HẾT PIN";
        if (pct < 50)  return "TRUNG BÌNH";
        return "TỐT";
    }

    // ── metric 2 : average speed ──────────────────────────────────────────────

    private double computeAverageSpeed(List<DroneDynamics> dynamics) {
        if (dynamics.isEmpty()) return 0;
        double sum = 0;
        for (DroneDynamics d : dynamics) sum += d.getSpeed();
        return sum / dynamics.size();
    }

    // ── metric 3 & 4 : connection state / active flag ─────────────────────────

    private String classifyConnection(DroneDynamics d) {
        if (d == null || d.getLastSeen() == null || d.getLastSeen().isBlank()) return "NO DATA";
        try {
            OffsetDateTime lastSeen = OffsetDateTime.parse(d.getLastSeen());
            long minutesAgo = ChronoUnit.MINUTES.between(lastSeen, OffsetDateTime.now());
            return minutesAgo <= 5 ? "ONLINE" : "STALE";
        } catch (DateTimeParseException e) {
            return "UNKNOWN";
        }
    }
}
