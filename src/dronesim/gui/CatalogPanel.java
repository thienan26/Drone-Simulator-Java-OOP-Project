package dronesim.gui;

import dronesim.model.DroneType;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Drone Catalog panel — shows all drone models/types with their specifications.
 */
public class CatalogPanel extends JPanel {

    private final CatalogTableModel tableModel;
    private final JLabel            footerLabel = new JLabel("Total Models: 0");

    public CatalogPanel() {
        tableModel = new CatalogTableModel();
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(new Color(240, 242, 248));

        JLabel hint = new JLabel("Danh sách loại drone – tải tự động khi Dashboard làm mới.");
        hint.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        hint.setForeground(new Color(90, 100, 120));

        footerLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        footerLabel.setForeground(new Color(90, 100, 120));

        setLayout(new BorderLayout());
        add(hint,                    BorderLayout.NORTH);
        add(new JScrollPane(table),  BorderLayout.CENTER);
        add(footerLabel,             BorderLayout.SOUTH);
    }

    /**
     * Updates the catalog table with a fresh list of drone types.
     */
    public void setDroneTypes(List<DroneType> types) {
        tableModel.setData(types);
        footerLabel.setText("Total Models: " + types.size());
    }

    // ── inner table model ─────────────────────────────────────────────────────

    private static final class CatalogTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
            "ID", "Manufacturer", "Type Name", "Weight (g)",
            "Max Speed (km/h)", "Max Carriage (g)", "Battery (mAh)", "Control Range (m)"
        };

        private List<DroneType> rows = new ArrayList<>();

        void setData(List<DroneType> data) {
            this.rows = new ArrayList<>(data);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int col) { return COLUMNS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            DroneType t = rows.get(row);
            return switch (col) {
                case 0 -> t.getId();
                case 1 -> t.getManufacturer();
                case 2 -> t.getTypeName();
                case 3 -> t.getWeight();
                case 4 -> t.getMaxSpeed();
                case 5 -> t.getMaxCarriageWeight();
                case 6 -> t.getBatteryCapacity();
                case 7 -> t.getControlRange();
                default -> "";
            };
        }
    }
}
