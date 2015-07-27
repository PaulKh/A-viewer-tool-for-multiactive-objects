package views.builders;

import callbacks.CompatibilityDialogCallback;
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
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Paul on 30/04/15.
 */
public class QueuesDialogBuilder implements CompatibilityDialogCallback {
    private DeliveryQueueTableModel model;

    public Dialog buildQueueDialog(Frame owner, ActiveObject activeObject, long timePressed) {
        int numberOfDialogs = PreferencesHelper.getNumberOfDialogs();
        if (numberOfDialogs == 0) {
            return null;
        }
        JDialog dialog = new JDialog(owner);
        dialog.setTitle(activeObject.getIdentifier());
        dialog.setLocationByPlatform(true);
        dialog.setPreferredSize(new Dimension(400, 300));
        List<ThreadEvent> threadEvents = getDeliveredList(activeObject, timePressed);
        WrappedQueueCompatibilityData data = new WrappedQueueCompatibilityData(threadEvents);
        model = new DeliveryQueueTableModel(data);
        JTable table = new JTable(model);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.setDefaultRenderer(Object.class, new QueueDialogRenderer(data, timePressed));

        QueueCompatibilityButtonColumn buttonColumn = new QueueCompatibilityButtonColumn(table, 3, data, this);
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

    @Override
    public void dataUpdated(WrappedQueueCompatibilityData data) {
        model.updateData(data);
    }
}
