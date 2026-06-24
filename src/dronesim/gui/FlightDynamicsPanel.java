package dronesim.gui;

import dronesim.controller.DashboardController;
import dronesim.exception.ApiException;
import dronesim.model.Drone;
import dronesim.service.DroneDynamicsService.PagedDynamics;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Flight Dynamics panel with per-drone pagination.
 * Only the records for the currently visible page are fetched from the API.
 */
public class FlightDynamicsPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(FlightDynamicsPanel.class.getName());
    private static final int PAGE_SIZE = 20;

    private final DashboardController controller;
    private final DynamicsTableModel tableModel;
    private final JComboBox<DroneItem> droneSelector;
    private final JLabel pageLabel;
    private final JButton prevBtn;
    private final JButton nextBtn;
    private final JButton refreshBtn;

    private int currentPage = 1;
    private boolean hasNextPage = false;
    private boolean loading = false;

    public FlightDynamicsPanel(DashboardController controller) {
        this.controller = controller;
        this.tableModel = new DynamicsTableModel();

        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.getTableHeader().setReorderingAllowed(false);

        droneSelector = new JComboBox<>();
        droneSelector.setPreferredSize(new Dimension(250, 26));

        prevBtn    = new JButton("← Previous");
        nextBtn    = new JButton("Next →");
        refreshBtn = new JButton("Load / Refresh");
        pageLabel  = new JLabel("Page — of —");

        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);

        refreshBtn.addActionListener(e -> loadPage(1));
        prevBtn.addActionListener(e -> loadPage(currentPage - 1));
        nextBtn.addActionListener(e -> loadPage(currentPage + 1));

        droneSelector.addActionListener(e -> {
            if (!loading) loadPage(1);
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.add(new JLabel("Drone:"));
        toolbar.add(droneSelector);
        toolbar.add(refreshBtn);

        JPanel pager = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        pager.add(prevBtn);
        pager.add(pageLabel);
        pager.add(nextBtn);

        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(pager, BorderLayout.SOUTH);
    }

    /**
     * Populates the drone selector when a fresh drone list is received.
     */
    public void setDrones(List<Drone> drones) {
        loading = true;
        droneSelector.removeAllItems();
        for (Drone d : drones) {
            droneSelector.addItem(new DroneItem(d));
        }
        loading = false;
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private void loadPage(int page) {
        DroneItem selected = (DroneItem) droneSelector.getSelectedItem();
        if (selected == null) return;
        if (page < 1) return;

        int droneId = selected.drone.getId();
        setControlsEnabled(false);

        Thread worker = new Thread(() -> {
            try {
                PagedDynamics paged = controller.loadDynamicsPage(droneId, page, PAGE_SIZE);
                int newPage = page;
                boolean hasNext = paged.pagination.hasNext();

                SwingUtilities.invokeLater(() -> {
                    tableModel.setData(paged.items);
                    currentPage = newPage;
                    hasNextPage = hasNext;
                    int totalPages = paged.pagination.getCount() > 0
                            ? (int) Math.ceil(paged.pagination.getCount() / (double) PAGE_SIZE)
                            : currentPage;
                    pageLabel.setText("Page " + currentPage + " of " + totalPages);
                    prevBtn.setEnabled(currentPage > 1);
                    nextBtn.setEnabled(hasNextPage);
                    setControlsEnabled(true);
                });
            } catch (ApiException e) {
                LOGGER.warning("Failed to load dynamics page: " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(FlightDynamicsPanel.this,
                            e.getMessage(), "API Error", JOptionPane.ERROR_MESSAGE);
                    setControlsEnabled(true);
                });
            }
        }, "DynamicsPageThread");
        worker.setDaemon(true);
        worker.start();
    }

    private void setControlsEnabled(boolean enabled) {
        refreshBtn.setEnabled(enabled);
        droneSelector.setEnabled(enabled);
        prevBtn.setEnabled(enabled && currentPage > 1);
        nextBtn.setEnabled(enabled && hasNextPage);
    }

    // ── helper record ─────────────────────────────────────────────────────────

    private static final class DroneItem {
        final Drone drone;
        DroneItem(Drone d) { this.drone = d; }
        @Override public String toString() {
            return "Drone #" + drone.getId() + "  [" + drone.getSerialNumber() + "]";
        }
    }
}
