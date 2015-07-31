package views.table_models;

import model.ThreadEvent;
import supportModel.WrappedQueueCompatibilityData;

import javax.swing.table.AbstractTableModel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Paul on 04/05/15.
 */
public class DeliveryQueueTableModel extends AbstractTableModel {
    private WrappedQueueCompatibilityData compatibilityData;
    private String[] headers = {"#", "Event identifier", "Delivered time", "Compatibility"};

    public DeliveryQueueTableModel(WrappedQueueCompatibilityData compatibilityData) {
        this.compatibilityData = compatibilityData;
    }

    public void updateData(WrappedQueueCompatibilityData compatibilityData) {
        this.compatibilityData = compatibilityData;
        fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public int getRowCount() {
        return compatibilityData.getThreadEvents().size();
    }

    @Override
    public int getColumnCount() {
        return headers.length;
    }

    @Override
    public String getColumnName(int col) {
        return headers[col];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ThreadEvent threadEvent = compatibilityData.getThreadEvents().get(rowIndex);
        switch (columnIndex) {
            case 0:
                return rowIndex + 1;
            case 1:
                return threadEvent.getUniqueMethodName();
            case 2: {
                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.S");
                Date startDate = new Date(threadEvent.getDerivedTime());

                return dateFormat.format(startDate);
            }
            case 3: {
                if (compatibilityData.getCompatibilityIdentifierSelected() == -1)
                    return "Show compatibility information";
                else if (compatibilityData.getCompatibilityIdentifierSelected() == rowIndex) {
                    return "Hide compatibility information";
                } else {
                    ThreadEvent selectedThreadEvent = compatibilityData.getThreadEvents().get(compatibilityData.getCompatibilityIdentifierSelected());
                    if (selectedThreadEvent.getThread().getActiveObject().areEventsCompatible(selectedThreadEvent, threadEvent))
                        return "compatible";
                    else
                        return "NOT compatible";
                }
            }
        }
        return null;
    }
}
