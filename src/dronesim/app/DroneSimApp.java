package dronesim.app;

import dronesim.api.ApiClient;
import dronesim.api.DroneSimApiClient;
import dronesim.config.AppConfig;
import dronesim.controller.DashboardController;
import dronesim.gui.MainFrame;
import dronesim.service.DroneDynamicsService;
import dronesim.service.DroneService;
import dronesim.service.DroneTypeService;
import dronesim.service.MetricsService;
import dronesim.util.AppLogger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application entry point.
 * Wires together config, API client, services, controller, and GUI.
 */
public class DroneSimApp {

    public static void main(String[] args) {
        AppLogger.setup();

        // Apply system look-and-feel for a native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> {
            AppConfig config = AppConfig.load();

            ApiClient apiClient = new DroneSimApiClient(config);

            DroneTypeService droneTypeService   = new DroneTypeService(apiClient);
            DroneService droneService           = new DroneService(apiClient);
            DroneDynamicsService dynamicsService = new DroneDynamicsService(apiClient);
            MetricsService metricsService       = new MetricsService();

            DashboardController controller = new DashboardController(
                    config,
                    droneTypeService,
                    droneService,
                    dynamicsService,
                    metricsService
            );

            MainFrame frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    }
}
