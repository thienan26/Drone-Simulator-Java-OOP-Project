package dronesim.gui;

import dronesim.controller.DashboardController;
import dronesim.controller.DashboardController.FleetSummary;
import dronesim.model.Drone;
import dronesim.model.DroneMetric;
import dronesim.model.DroneType;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Dashboard panel with:
 * - 4 fleet-level summary cards at the top
 * - Sortable drone table with battery progress bar and colour-coded battery alert
 * - Drone detail side-panel that updates on row selection
 */
public class DashboardPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(DashboardPanel.class.getName());

    // ── colours ───────────────────────────────────────────────────────────────
    private static final Color COLOR_CARD_BG    = new Color(245, 247, 252);
    private static final Color COLOR_BORDER     = new Color(220, 225, 235);
    private static final Color COLOR_OK         = new Color(34, 197, 94);
    private static final Color COLOR_WARN       = new Color(249, 115, 22);
    private static final Color COLOR_DANGER     = new Color(239, 68, 68);
    private static final Color COLOR_BLUE       = new Color(59, 130, 246);

    private final DashboardController controller;
    private final DroneTableModel     tableModel;
    private final JTable              table;
    private final JLabel              statusLabel;

    // Summary card value labels
    private final JLabel lblOnline     = bigValueLabel("—");
    private final JLabel lblLowBat     = bigValueLabel("—");
    private final JLabel lblAvgSpeed   = bigValueLabel("—");
    private final JLabel lblTopDrone   = bigValueLabel("—");

    // Detail panel labels
    private final JLabel detSerial     = detailValue("—");
    private final JLabel detType       = detailValue("—");
    private final JLabel detStatus     = detailValue("—");
    private final JLabel detBattery    = detailValue("—");
    private final JLabel detBatAlert   = detailValue("—");
    private final JLabel detSpeed      = detailValue("—");
    private final JLabel detConnection = detailValue("—");

    private Map<Integer, DroneType>   currentTypes   = new HashMap<>();
    private Map<Integer, DroneMetric> currentMetrics = new HashMap<>();

    public DashboardPanel(DashboardController controller) {
        this.controller = controller;
        this.tableModel = new DroneTableModel();
        this.table      = buildTable();
        this.statusLabel = new JLabel("Chưa có dữ liệu.");

        setLayout(new BorderLayout(0, 0));
        add(buildTopBar(),      BorderLayout.NORTH);
        add(buildCenterSplit(), BorderLayout.CENTER);
    }

    // ── public update methods (called by MainFrame) ───────────────────────────

    public void setDrones(List<Drone> drones) {
        currentMetrics.clear();
        tableModel.setData(drones, currentTypes, currentMetrics);
        statusLabel.setText(drones.size() + " drone(s) – đang tải metrics…");
        loadMetricsAsync(drones);
    }

    public void setTypes(Map<Integer, DroneType> types) {
        this.currentTypes = types;
    }

    // ── build UI ──────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        // Toolbar row
        JButton refreshBtn     = new JButton("⟳  Làm mới");
        JToggleButton autoBtn  = new JToggleButton("Auto-refresh: TẮT");
        refreshBtn.addActionListener(e -> controller.refreshDashboard());
        autoBtn.addActionListener(e -> {
            if (autoBtn.isSelected()) {
                controller.startAutoRefresh();
                autoBtn.setText("Auto-refresh: BẬT");
            } else {
                controller.stopAutoRefresh();
                autoBtn.setText("Auto-refresh: TẮT");
            }
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        toolbar.setOpaque(false);
        toolbar.add(refreshBtn);
        toolbar.add(autoBtn);
        toolbar.add(statusLabel);

        // Summary cards row
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
        cards.add(summaryCard("Drone Đang Hoạt Động", lblOnline,  "tổng số",  COLOR_BLUE));
        cards.add(summaryCard("Cảnh Báo Pin Yếu",     lblLowBat,  "cần chú ý", COLOR_DANGER));
        cards.add(summaryCard("Tốc Độ TB Cả Đội",     lblAvgSpeed,"km/h",      COLOR_OK));
        cards.add(summaryCard("Top Drone (Tốc Độ)",   lblTopDrone,"ID nhanh nhất", COLOR_WARN));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.add(toolbar, BorderLayout.NORTH);
        top.add(cards,   BorderLayout.CENTER);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        return top;
    }

    private JSplitPane buildCenterSplit() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(table), buildDetailPanel());
        split.setResizeWeight(0.75);
        split.setDividerLocation(0.75);
        split.setBorder(null);
        return split;
    }

    private JTable buildTable() {
        JTable t = new JTable(tableModel);
        t.setAutoCreateRowSorter(true);
        t.setFillsViewportHeight(true);
        t.setRowHeight(26);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setBackground(new Color(240, 242, 248));
        t.setSelectionBackground(new Color(219, 234, 254));
        t.setSelectionForeground(Color.BLACK);

        // Battery progress bar renderer
        t.getColumnModel().getColumn(DroneTableModel.COL_BATTERY_PCT)
                .setCellRenderer(new BatteryBarRenderer());

        // Battery alert colour renderer
        t.getColumnModel().getColumn(DroneTableModel.COL_BATTERY_ALERT)
                .setCellRenderer(new BatteryAlertRenderer());

        // Connection state colour renderer
        t.getColumnModel().getColumn(DroneTableModel.COL_CONNECTION)
                .setCellRenderer(new ConnectionRenderer());

        // Alternating row colour
        t.setDefaultRenderer(Object.class, new AlternatingRowRenderer());

        // Update detail panel on selection
        t.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateDetailPanel();
        });

        return t;
    }

    private JPanel buildDetailPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(COLOR_CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, COLOR_BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        p.add(sectionTitle("Chi Tiết Drone"));
        p.add(Box.createVerticalStrut(10));
        p.add(detailRow("Serial:",      detSerial));
        p.add(detailRow("Loại:",        detType));
        p.add(detailRow("Trạng thái:",  detStatus));
        p.add(detailRow("Pin:",         detBattery));
        p.add(detailRow("Cảnh báo:",    detBatAlert));
        p.add(detailRow("Tốc độ TB:",   detSpeed));
        p.add(detailRow("Kết nối:",     detConnection));
        p.add(Box.createVerticalGlue());
        return p;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void loadMetricsAsync(List<Drone> drones) {
        Thread worker = new Thread(() -> {
            for (Drone d : drones) {
                DroneMetric m = controller.calculateMetrics(d);
                if (m != null) {
                    currentMetrics.put(d.getId(), m);
                    SwingUtilities.invokeLater(() ->
                            tableModel.setData(drones, currentTypes, currentMetrics));
                }
            }
            SwingUtilities.invokeLater(() -> {
                FleetSummary fs = controller.computeFleetSummary(
                        currentMetrics.values(), drones.size());
                updateSummaryCards(fs);
                statusLabel.setText(drones.size() + " drone(s) | metrics đã tải xong.");
            });
            LOGGER.info("Metrics loaded for all drones");
        }, "MetricsLoaderThread");
        worker.setDaemon(true);
        worker.start();
    }

    private void updateSummaryCards(FleetSummary fs) {
        lblOnline  .setText(fs.onlineDrones + " / " + fs.totalDrones);
        lblLowBat  .setText(String.valueOf(fs.lowBatteryCount));
        lblAvgSpeed.setText(String.format("%.1f", fs.avgFleetSpeed));
        lblTopDrone.setText(fs.topSpeedDroneId > 0
                ? "#" + fs.topSpeedDroneId + "  (" + String.format("%.1f", fs.topSpeed) + " km/h)"
                : "—");
    }

    private void updateDetailPanel() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        Drone d      = tableModel.getDroneAt(modelRow);
        DroneType t  = currentTypes.get(d.getDroneTypeId());
        DroneMetric m = currentMetrics.get(d.getId());

        detSerial    .setText(d.getSerialNumber());
        detType      .setText(t != null ? t.getManufacturer() + " " + t.getTypeName() : "—");
        detStatus    .setText(m != null ? m.getStatus()    : "—");
        detBattery   .setText(m != null ? String.format("%.0f%%", m.getBatteryPercent()) : "—");
        detBatAlert  .setText(m != null ? m.getBatteryAlert()  : "—");
        detSpeed     .setText(m != null ? String.format("%.1f km/h", m.getAverageSpeed()) : "—");
        detConnection.setText(m != null ? m.getConnectionState() : "—");

        if (m != null) {
            Color alertColor = switch (m.getBatteryAlert()) {
                case "SẮP HẾT PIN" -> COLOR_DANGER;
                case "TRUNG BÌNH"  -> COLOR_WARN;
                default            -> COLOR_OK;
            };
            detBatAlert.setForeground(alertColor);
        }
    }

    // ── factory helpers ───────────────────────────────────────────────────────

    private JPanel summaryCard(String title, JLabel valueLabel, String subtitle, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(titleLbl.getFont().deriveFont(Font.PLAIN, 11f));
        titleLbl.setForeground(new Color(100, 110, 130));

        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 22f));
        valueLabel.setForeground(accent);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(subLbl.getFont().deriveFont(Font.PLAIN, 10f));
        subLbl.setForeground(new Color(150, 160, 180));

        card.add(titleLbl,  BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLbl,    BorderLayout.SOUTH);
        return card;
    }

    private JPanel detailRow(String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(100, 110, 130));
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11f));
        row.add(lbl,   BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        return row;
    }

    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private static JLabel bigValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 22f));
        return l;
    }

    private static JLabel detailValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        return l;
    }

    // ── cell renderers ────────────────────────────────────────────────────────

    /** Renders battery percentage as a coloured JProgressBar. */
    private static final class BatteryBarRenderer extends JProgressBar implements TableCellRenderer {
        BatteryBarRenderer() {
            setMinimum(0); setMaximum(100); setStringPainted(true);
        }
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {
            int pct = value instanceof Number ? (int) ((Number) value).doubleValue() : 0;
            if (pct < 0) { setString("—"); setValue(0); return this; }
            setValue(pct);
            setString(pct + "%");
            if      (pct < 20) setForeground(new Color(220, 50, 50));
            else if (pct < 50) setForeground(new Color(220, 140, 30));
            else               setForeground(new Color(34, 150, 80));
            return this;
        }
    }

    /** Colours the battery alert column: SẮP HẾT PIN=red, TRUNG BÌNH=orange, TỐT=green. */
    private static final class BatteryAlertRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(CENTER);
            if (!isSelected) {
                String v = value == null ? "" : value.toString();
                switch (v) {
                    case "SẮP HẾT PIN" -> { setBackground(new Color(255,220,220)); setForeground(new Color(180,30,30)); }
                    case "TRUNG BÌNH"  -> { setBackground(new Color(255,240,200)); setForeground(new Color(160,100,0)); }
                    case "TỐT"         -> { setBackground(new Color(210,245,220)); setForeground(new Color(30,130,60)); }
                    default            -> { setBackground(Color.WHITE);            setForeground(Color.DARK_GRAY); }
                }
            }
            return this;
        }
    }

    /** Colours the connection state column: ONLINE=green, STALE=orange. */
    private static final class ConnectionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(CENTER);
            if (!isSelected) {
                String v = value == null ? "" : value.toString();
                switch (v) {
                    case "ONLINE"  -> { setBackground(new Color(210,245,220)); setForeground(new Color(30,130,60)); }
                    case "STALE"   -> { setBackground(new Color(255,240,200)); setForeground(new Color(160,100,0)); }
                    default        -> { setBackground(Color.WHITE);            setForeground(Color.GRAY); }
                }
            }
            return this;
        }
    }

    /** Alternating row background for non-specialised columns. */
    private static final class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (!isSelected) {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 255));
                setForeground(Color.DARK_GRAY);
            }
            return this;
        }
    }
}
