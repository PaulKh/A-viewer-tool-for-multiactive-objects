package views.renderers;

import model.ThreadEvent;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by Paul on 04/05/15.
 */
public class QueueDialogRenderer implements TableCellRenderer {
    private List<ThreadEvent> threadEvents;
    private long timeSelected;
    public QueueDialogRenderer(java.util.List<ThreadEvent> threadEvents, long timeSelected) {
        this.threadEvents = threadEvents;
        this.timeSelected = timeSelected;
    }
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JTextField editor = new JTextField();
        editor.setBorder(null);
        if (value != null)
            editor.setText(value.toString());
        if (threadEvents.get(row).getFinishTime() < timeSelected){
            editor.setBackground(Color.RED);
        }
        else if(threadEvents.get(row).getStartTime() < timeSelected){
            editor.setBackground(Color.GREEN);
        }
        else if(threadEvents.get(row).getDerivedTime() < timeSelected){
            editor.setBackground(Color.cyan);
        }
        return editor;
    }
}
