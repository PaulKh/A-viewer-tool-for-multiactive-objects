package views.builders;

import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.WrappedQueueCompatibilityData;
import utils.PreferencesHelper;
import views.renderers.QueueCompatibilityButtonColumn;
import views.renderers.QueueDialogRenderer;
import views.table_models.DeliveryQueueTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Paul on 30/04/15.
 */
public class QueuesDialogBuilder {
    private static List<WrappedQueueCompatibilityData> dialogs = new ArrayList<>();

    public Dialog buildQueueDialog(Frame owner, ActiveObject activeObject, long timePressed) {
        int numberOfDialogs = PreferencesHelper.getNumberOfDialogs();
        if (numberOfDialogs == 0) {
            return null;
        }

        WrappedQueueCompatibilityData dialog = new WrappedQueueCompatibilityData(owner);
        if (dialogs.size() >= numberOfDialogs) {
            dialogs.get(0).dispose();
            dialogs.remove(0);
        }
        dialogs.add(dialog);
        dialog.setTitle(activeObject.getIdentifier());
        dialog.setLocationByPlatform(true);

        dialog.setPreferredSize(new Dimension(400, 300));
        List<ThreadEvent> threadEvents = getDeliveredList(activeObject, timePressed);
        JTable table = new JTable(new DeliveryQueueTableModel(threadEvents));
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.setDefaultRenderer(Object.class, new QueueDialogRenderer(threadEvents, timePressed));
//        table.getColumn("Compatibility").setCellRenderer(new ButtonRenderer());
//        table.getColumn("Compatibility").setCellEditor(
//                new ButtonEditor(new JCheckBox()));
        Action showCompatibility = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {

            }
        };
        QueueCompatibilityButtonColumn buttonColumn = new QueueCompatibilityButtonColumn(table, showCompatibility, 3, threadEvents);
        buttonColumn.setMnemonic(KeyEvent.VK_D);
        JScrollPane scrollPane = new JScrollPane(table);
        dialog.add(scrollPane);
        dialog.pack();
        dialog.setVisible(true);
        return dialog;
    }

    private static List<ThreadEvent> getDeliveredList(ActiveObject activeObject, long timePressed) {
        List<ThreadEvent> allThreadEventsToSort = new ArrayList<>();
        for (ActiveObjectThread thread : activeObject.getThreads()) {
            allThreadEventsToSort.addAll(thread.getEvents());
//            for (ThreadEvent threadEvent:thread.getEvents()){
//                if (threadEvent.getDerivedTime() > timePressed){
//                    allThreadEventsToSort.add(threadEvent);
//                }
//            }
        }
        Collections.sort(allThreadEventsToSort, new Comparator<ThreadEvent>() {
            @Override
            public int compare(ThreadEvent o1, ThreadEvent o2) {
                return o1.getDerivedTime() > o2.getDerivedTime() ? 1 : -1;
            }
        });
        return allThreadEventsToSort;
    }
}
