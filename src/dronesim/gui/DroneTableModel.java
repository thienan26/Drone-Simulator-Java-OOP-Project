package dronesim.gui;

import dronesim.model.Drone;
import dronesim.model.DroneMetric;
import dronesim.model.DroneType;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Table model for the Dashboard – one row per drone, columns match the 4 new metrics.
 */
public class DroneTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
        "ID", "Serial Number", "Loại Drone", "Trạng thái",
        "Pin (%)", "Cảnh báo Pin", "Tốc độ TB (km/h)", "Kết nối"
    };

    /** Column indices – used by the cell renderer to locate specific columns. */
    static final int COL_BATTERY_PCT  = 4;
    static final int COL_BATTERY_ALERT = 5;
    static final int COL_CONNECTION   = 7;

    private List<Drone> drones = new ArrayList<>();
    private Map<Integer, DroneType>   types;
    private Map<Integer, DroneMetric> metrics;

    /**
     * Replaces the full dataset and notifies listeners to repaint.
     */
    public void setData(List<Drone> drones,
                        Map<Integer, DroneType>   types,
                        Map<Integer, DroneMetric> metrics) {
        this.drones  = new ArrayList<>(drones);
        this.types   = types;
        this.metrics = metrics;
        fireTableDataChanged();
    }

    @Override public int    getRowCount()              { return drones.size(); }
    @Override public int    getColumnCount()           { return COLUMNS.length; }
    @Override public String getColumnName(int col)     { return COLUMNS[col]; }
    @Override public Class<?> getColumnClass(int col)  {
        return col == COL_BATTERY_PCT ? Double.class : Object.class;
    }

    @Override
    public Object getValueAt(int row, int col) {
        Drone      d = drones.get(row);
        DroneType  t = types   != null ? types.get(d.getDroneTypeId())  : null;
        DroneMetric m = metrics != null ? metrics.get(d.getId())         : null;

        return switch (col) {
            case 0 -> d.getId();
            case 1 -> d.getSerialNumber();
            case 2 -> t != null ? t.getManufacturer() + " " + t.getTypeName()
                                : "Type #" + d.getDroneTypeId();
            case 3 -> m != null ? m.getStatus()    : "—";
            case COL_BATTERY_PCT   -> m != null ? m.getBatteryPercent() : -1.0;
            case COL_BATTERY_ALERT -> m != null ? m.getBatteryAlert()   : "—";
            case 6 -> m != null ? String.format("%.1f", m.getAverageSpeed()) : "—";
            case COL_CONNECTION    -> m != null ? m.getConnectionState() : "—";
            default -> "";
        };
    }

    /** Returns the Drone at the given view row. */
    public Drone getDroneAt(int row) { return drones.get(row); }
}
