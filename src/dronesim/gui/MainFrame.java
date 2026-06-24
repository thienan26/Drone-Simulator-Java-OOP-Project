package dronesim.gui;

import dronesim.controller.DashboardController;
import dronesim.model.DroneType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Main application window.
 * Layout: dark left sidebar (navigation) + CardLayout main content area.
 */
public class MainFrame extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());

    // ── sidebar colours ───────────────────────────────────────────────────────
    private static final Color SIDEBAR_BG      = new Color(25,  35,  55);
    private static final Color SIDEBAR_HOVER   = new Color(40,  55,  85);
    private static final Color SIDEBAR_ACTIVE  = new Color(59, 130, 246);
    private static final Color SIDEBAR_FG      = new Color(190, 200, 220);
    private static final Color SIDEBAR_FG_MUTE = new Color(110, 125, 155);

    // ── card names ─────────────────────────────────────────────────────────────
    private static final String CARD_DASHBOARD = "Dashboard";
    private static final String CARD_CATALOG   = "Drone Catalog";
    private static final String CARD_DYNAMICS  = "Flight Dynamics";
    private static final String CARD_SETTINGS  = "Settings";

    private final DashboardController    controller;
    private final DashboardPanel         dashboardPanel;
    private final CatalogPanel           catalogPanel;
    private final FlightDynamicsPanel    flightDynamicsPanel;

    private final CardLayout  cardLayout  = new CardLayout();
    private final JPanel      contentArea = new JPanel(cardLayout);
    private       JButton     activeNavBtn;

    public MainFrame(DashboardController controller) {
        this.controller          = controller;
        this.dashboardPanel      = new DashboardPanel(controller);
        this.catalogPanel        = new CatalogPanel();
        this.flightDynamicsPanel = new FlightDynamicsPanel(controller);

        setTitle("Drone Simulation Interface");
        setSize(1200, 720);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                controller.stopAutoRefresh();
                LOGGER.info("Application closing");
                dispose();
                System.exit(0);
            }
        });

        // ── layout ────────────────────────────────────────────────────────────
        setLayout(new BorderLayout());
        add(buildSidebar(),   BorderLayout.WEST);
        add(contentArea,      BorderLayout.CENTER);

        // ── register cards ────────────────────────────────────────────────────
        contentArea.add(dashboardPanel,      CARD_DASHBOARD);
        contentArea.add(catalogPanel,        CARD_CATALOG);
        contentArea.add(flightDynamicsPanel, CARD_DYNAMICS);
        contentArea.add(buildSettingsCard(), CARD_SETTINGS);

        // ── wire controller callbacks ─────────────────────────────────────────
        controller.setOnTypeListUpdated(types -> {
            Map<Integer, DroneType> typeMap = new HashMap<>();
            for (DroneType t : types) typeMap.put(t.getId(), t);
            dashboardPanel.setTypes(typeMap);
            catalogPanel.setDroneTypes(types);
        });

        controller.setOnDroneListUpdated(drones -> {
            dashboardPanel.setDrones(drones);
            flightDynamicsPanel.setDrones(drones);
        });

        controller.setOnError(msg ->
                JOptionPane.showMessageDialog(this, msg, "Lỗi kết nối", JOptionPane.ERROR_MESSAGE));

        // ── initial load ──────────────────────────────────────────────────────
        if (controller.getConfig().isValid()) {
            controller.refreshDashboard();
        } else {
            SwingUtilities.invokeLater(() -> showSettings());
        }
    }

    // ── sidebar ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(200, 0));

        // App title / logo area
        JPanel logoArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        logoArea.setOpaque(false);
        logoArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        JLabel logoIcon  = new JLabel("◈");
        logoIcon.setFont(logoIcon.getFont().deriveFont(Font.BOLD, 20f));
        logoIcon.setForeground(SIDEBAR_ACTIVE);
        JLabel logoTitle = new JLabel("DroneSimApp");
        logoTitle.setFont(logoTitle.getFont().deriveFont(Font.BOLD, 13f));
        logoTitle.setForeground(Color.WHITE);
        logoArea.add(logoIcon);
        logoArea.add(logoTitle);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(logoArea);
        sidebar.add(Box.createVerticalStrut(10));

        // Separator
        sidebar.add(hLine());
        sidebar.add(Box.createVerticalStrut(8));

        // Navigation items
        JButton btnDash     = navButton("⊞  Dashboard",       CARD_DASHBOARD);
        JButton btnCatalog  = navButton("☰  Drone Catalog",   CARD_CATALOG);
        JButton btnDynamic  = navButton("✈  Flight Dynamics", CARD_DYNAMICS);
        JButton btnSettings = navButton("⚙  Settings",        CARD_SETTINGS);

        sidebar.add(btnDash);
        sidebar.add(btnCatalog);
        sidebar.add(btnDynamic);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(hLine());
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(btnSettings);
        sidebar.add(Box.createVerticalStrut(12));

        // Activate the dashboard button by default
        activateNav(btnDash, CARD_DASHBOARD);
        return sidebar;
    }

    private JButton navButton(String label, String card) {
        JButton btn = new JButton(label);
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBackground(SIDEBAR_BG);
        btn.setForeground(SIDEBAR_FG);
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 13f));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != activeNavBtn) btn.setBackground(SIDEBAR_HOVER);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != activeNavBtn) btn.setBackground(SIDEBAR_BG);
            }
        });

        btn.addActionListener(e -> activateNav(btn, card));
        return btn;
    }

    private void activateNav(JButton btn, String card) {
        if (activeNavBtn != null) {
            activeNavBtn.setBackground(SIDEBAR_BG);
            activeNavBtn.setForeground(SIDEBAR_FG);
        }
        activeNavBtn = btn;
        btn.setBackground(SIDEBAR_ACTIVE);
        btn.setForeground(Color.WHITE);

        if (CARD_SETTINGS.equals(card)) {
            showSettings();
        } else {
            cardLayout.show(contentArea, card);
        }
    }

    private void showSettings() {
        new SettingsDialog(this, controller).setVisible(true);
        // After settings dialog closes, return visual focus to previous card
        if (activeNavBtn != null) {
            String card = activeNavBtn.getText().contains("Dashboard")   ? CARD_DASHBOARD
                        : activeNavBtn.getText().contains("Catalog")     ? CARD_CATALOG
                        : activeNavBtn.getText().contains("Flight")      ? CARD_DYNAMICS
                        : CARD_DASHBOARD;
            cardLayout.show(contentArea, card);
        }
    }

    private JPanel hLine() {
        JPanel line = new JPanel();
        line.setBackground(SIDEBAR_FG_MUTE);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return line;
    }

    /** Placeholder panel shown in the Settings card slot (actual settings use a dialog). */
    private JPanel buildSettingsCard() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(245, 247, 252));
        JLabel lbl = new JLabel("Mở cửa sổ cài đặt…", SwingConstants.CENTER);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 14f));
        lbl.setForeground(new Color(120, 130, 150));
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }
}
