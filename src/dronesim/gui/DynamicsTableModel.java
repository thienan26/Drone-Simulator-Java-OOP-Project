package dronesim.gui;

import dronesim.model.DroneDynamics;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for the Flight Dynamics panel showing one page of dynamics at a time.
 */
public class DynamicsTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
        "Timestamp", "Speed (km/h)", "Latitude", "Longitude",
        "Battery", "Status", "Last Seen", "Roll", "Pitch", "Yaw"
    };

    private List<DroneDynamics> rows = new ArrayList<>();

    /**
     * Replaces the current page of dynamics and repaints.
     */
    public void setData(List<DroneDynamics> data) {
        this.rows = new ArrayList<>(data);
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        DroneDynamics d = rows.get(row);
        return switch (col) {
            case 0 -> d.getTimestamp();
            case 1 -> d.getSpeed();
            case 2 -> String.format("%.6f", d.getLatitude());
            case 3 -> String.format("%.6f", d.getLongitude());
            case 4 -> d.getBatteryStatus();
            case 5 -> d.getStatus();
            case 6 -> d.getLastSeen();
            case 7 -> String.format("%.2f", d.getAlignRoll());
            case 8 -> String.format("%.2f", d.getAlignPitch());
            case 9 -> String.format("%.2f", d.getAlignYaw());
            default -> "";
        };
    }
}
