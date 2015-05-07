package views.table_models;

import model.ThreadEvent;

import javax.swing.table.AbstractTableModel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Paul on 04/05/15.
 */
public class DeliveryQueueTableModel extends AbstractTableModel{
    private List<ThreadEvent> threadEvents;
    private String[] headers = {"#", "Event identifier", "Delivered time"};

    public DeliveryQueueTableModel(List<ThreadEvent> threadEvents) {
        this.threadEvents = threadEvents;
    }

    @Override
    public int getRowCount() {
        return threadEvents.size();
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
        switch (columnIndex){
            case 0:
                return rowIndex + 1;
            case 1:
                return threadEvents.get(rowIndex).getUniqueMethodName();
            case 2: {
                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.S");
                Date startDate = new Date(threadEvents.get(rowIndex).getDerivedTime());

                return dateFormat.format(startDate);
            }
        }
        return null;
    }
}
