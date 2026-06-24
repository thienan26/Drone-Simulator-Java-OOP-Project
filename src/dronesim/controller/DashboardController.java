package dronesim.controller;

import dronesim.config.AppConfig;
import dronesim.exception.ApiException;
import dronesim.model.Drone;
import dronesim.model.DroneDynamics;
import dronesim.model.DroneMetric;
import dronesim.model.DroneType;
import dronesim.service.DroneDynamicsService;
import dronesim.service.DroneDynamicsService.PagedDynamics;
import dronesim.service.DroneService;
import dronesim.service.DroneTypeService;
import dronesim.service.MetricsService;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Mediates between the GUI and the service layer.
 * All network calls run on background threads; GUI callbacks are dispatched via SwingUtilities.invokeLater.
 */
public class DashboardController {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());
    private static final int RECENT_DYNAMICS_COUNT = 20;

    private final AppConfig config;
    private final DroneTypeService droneTypeService;
    private final DroneService droneService;
    private final DroneDynamicsService dynamicsService;
    private final MetricsService metricsService;

    private List<Drone> cachedDrones = new ArrayList<>();
    private Map<Integer, DroneType> cachedTypes = new HashMap<>();

    private Consumer<List<Drone>>     onDroneListUpdated;
    private Consumer<List<DroneType>> onTypeListUpdated;
    private Consumer<String>          onError;

    private Thread  autoRefreshThread;
    private volatile boolean autoRefreshRunning = false;
    private int autoRefreshIntervalSeconds = 60;

    public DashboardController(AppConfig config,
                               DroneTypeService droneTypeService,
                               DroneService droneService,
                               DroneDynamicsService dynamicsService,
                               MetricsService metricsService) {
        this.config            = config;
        this.droneTypeService  = droneTypeService;
        this.droneService      = droneService;
        this.dynamicsService   = dynamicsService;
        this.metricsService    = metricsService;
    }

    // ── GUI callbacks ─────────────────────────────────────────────────────────

    public void setOnDroneListUpdated(Consumer<List<Drone>>     cb) { this.onDroneListUpdated = cb; }
    public void setOnTypeListUpdated(Consumer<List<DroneType>>  cb) { this.onTypeListUpdated  = cb; }
    public void setOnError(Consumer<String>                     cb) { this.onError             = cb; }

    // ── dashboard refresh ─────────────────────────────────────────────────────

    /**
     * Fetches drone types and drones on a background thread, then notifies GUI callbacks.
     */
    public void refreshDashboard() {
        Thread worker = new Thread(() -> {
            try {
                List<DroneType> types = droneTypeService.getAll();
                cachedTypes.clear();
                for (DroneType t : types) cachedTypes.put(t.getId(), t);

                List<Drone> drones = droneService.getAll();
                cachedDrones = new ArrayList<>(drones);

                SwingUtilities.invokeLater(() -> {
                    if (onTypeListUpdated  != null) onTypeListUpdated.accept(types);
                    if (onDroneListUpdated != null) onDroneListUpdated.accept(drones);
                });
            } catch (ApiException e) {
                LOGGER.warning("Dashboard refresh failed: " + e.getMessage());
                String msg = e.getMessage();
                SwingUtilities.invokeLater(() -> { if (onError != null) onError.accept(msg); });
            }
        }, "DashboardRefreshThread");
        worker.setDaemon(true);
        worker.start();
    }

    // ── metrics ───────────────────────────────────────────────────────────────

    /**
     * Fetches recent dynamics for one drone and computes its four metrics.
     * Returns null when no dynamics data is available.
     */
    public DroneMetric calculateMetrics(Drone drone) {
        try {
            List<DroneDynamics> recent =
                    dynamicsService.getRecentForDrone(drone.getId(), RECENT_DYNAMICS_COUNT);
            if (recent.isEmpty()) return null;
            DroneType type = cachedTypes.get(drone.getDroneTypeId());
            return metricsService.calculate(drone, type, recent);
        } catch (ApiException e) {
            LOGGER.warning("Metrics fetch failed for drone " + drone.getId() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Computes fleet-wide summary statistics from already-loaded drone metrics.
     * Call this after all per-drone metrics have been calculated.
     */
    public FleetSummary computeFleetSummary(Collection<DroneMetric> allMetrics,
                                            int totalDrones) {
        int    onlineCount      = 0;
        int    lowBatteryCount  = 0;
        double totalSpeed       = 0;
        int    speedCount       = 0;

        int    topSpeedDroneId     = -1;
        double topSpeed            = -1;
        int    topBatteryDroneId   = -1;
        double topBatteryPct       = -1;

        for (DroneMetric m : allMetrics) {
            if (m.isActive())                           onlineCount++;
            if ("SẮP HẾT PIN".equals(m.getBatteryAlert())) lowBatteryCount++;

            if (m.getAverageSpeed() > 0) {
                totalSpeed += m.getAverageSpeed();
                speedCount++;
            }
            if (m.getAverageSpeed() > topSpeed) {
                topSpeed        = m.getAverageSpeed();
                topSpeedDroneId = m.getDroneId();
            }
            if (m.getBatteryPercent() > topBatteryPct) {
                topBatteryPct       = m.getBatteryPercent();
                topBatteryDroneId   = m.getDroneId();
            }
        }

        double avgFleetSpeed = speedCount > 0 ? totalSpeed / speedCount : 0;
        return new FleetSummary(totalDrones, onlineCount, lowBatteryCount,
                                avgFleetSpeed, topSpeedDroneId, topSpeed,
                                topBatteryDroneId, topBatteryPct);
    }

    // ── flight dynamics pagination ────────────────────────────────────────────

    /**
     * Loads a single page of dynamics for the given drone.
     */
    public PagedDynamics loadDynamicsPage(int droneId, int page, int pageSize) throws ApiException {
        return dynamicsService.getPageForDrone(droneId, page, pageSize);
    }

    // ── auto-refresh ──────────────────────────────────────────────────────────

    /** Starts background auto-refresh at the configured interval. */
    public void startAutoRefresh() {
        if (autoRefreshRunning) return;
        autoRefreshRunning = true;
        autoRefreshThread = new Thread(() -> {
            while (autoRefreshRunning && !Thread.currentThread().isInterrupted()) {
                refreshDashboard();
                try {
                    Thread.sleep(autoRefreshIntervalSeconds * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "AutoRefreshThread");
        autoRefreshThread.setDaemon(true);
        autoRefreshThread.start();
        LOGGER.info("Auto-refresh started (interval: " + autoRefreshIntervalSeconds + "s)");
    }

    /** Stops the auto-refresh background thread. */
    public void stopAutoRefresh() {
        autoRefreshRunning = false;
        if (autoRefreshThread != null) autoRefreshThread.interrupt();
        LOGGER.info("Auto-refresh stopped");
    }

    // ── settings ──────────────────────────────────────────────────────────────

    /** Persists new settings then triggers a full dashboard reload. */
    public void applySettings(String baseUrl, String token) {
        config.setBaseUrl(baseUrl);
        config.setToken(token);
        config.save();
        refreshDashboard();
    }

    // ── accessors ─────────────────────────────────────────────────────────────

    public AppConfig              getConfig()        { return config; }
    public List<Drone>            getCachedDrones()  { return cachedDrones; }
    public Map<Integer, DroneType> getCachedTypes()  { return cachedTypes; }
    public boolean                isAutoRefreshRunning() { return autoRefreshRunning; }
    public void setAutoRefreshInterval(int s)        { this.autoRefreshIntervalSeconds = s; }

    // ── fleet summary DTO ─────────────────────────────────────────────────────

    /** Immutable carrier for fleet-level aggregate statistics. */
    public static final class FleetSummary {
        public final int    totalDrones;
        public final int    onlineDrones;
        public final int    lowBatteryCount;
        public final double avgFleetSpeed;
        public final int    topSpeedDroneId;
        public final double topSpeed;
        public final int    topBatteryDroneId;
        public final double topBatteryPercent;

        FleetSummary(int total, int online, int lowBat, double avgSpeed,
                     int topSpeedId, double topSpeed, int topBatId, double topBatPct) {
            this.totalDrones      = total;
            this.onlineDrones     = online;
            this.lowBatteryCount  = lowBat;
            this.avgFleetSpeed    = avgSpeed;
            this.topSpeedDroneId  = topSpeedId;
            this.topSpeed         = topSpeed;
            this.topBatteryDroneId = topBatId;
            this.topBatteryPercent = topBatPct;
        }
    }
}
