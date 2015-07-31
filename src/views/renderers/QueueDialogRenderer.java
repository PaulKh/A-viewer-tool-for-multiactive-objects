package views.renderers;

import supportModel.WrappedQueueCompatibilityData;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by Paul on 04/05/15.
 */
public class QueueDialogRenderer implements TableCellRenderer {
    private WrappedQueueCompatibilityData compatibilityData;
    private long timeSelected;

    public QueueDialogRenderer(WrappedQueueCompatibilityData compatibilityData, long timeSelected) {
        this.compatibilityData = compatibilityData;
        this.timeSelected = timeSelected;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JTextField editor = new JTextField();
        editor.setBorder(null);
        if (value != null)
            editor.setText(value.toString());
        if (compatibilityData.getThreadEvents().get(row).getFinishTime() < timeSelected) {
            editor.setBackground(Color.RED);
        } else if (compatibilityData.getThreadEvents().get(row).getStartTime() < timeSelected) {
            editor.setBackground(Color.GREEN);
        } else if (compatibilityData.getThreadEvents().get(row).getDerivedTime() < timeSelected) {
            editor.setBackground(Color.cyan);
        }
        return editor;
    }
}
