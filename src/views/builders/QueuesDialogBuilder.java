package views.builders;

import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import utils.PreferencesHelper;
import views.MainWindow;
import views.renderers.QueueDialogRenderer;
import views.table_models.DeliveryQueueTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Paul on 30/04/15.
 */
public class QueuesDialogBuilder {
    private static List<Dialog> dialogs = new ArrayList<>();

    public static Dialog buildQueueDialog(Frame owner, ActiveObject activeObject, long timePressed) {
        int numberOfDialogs = PreferencesHelper.getNumberOfDialogs(MainWindow.class);
        if (numberOfDialogs == 0) {
            return null;
        }

        JDialog dialog = new JDialog(owner);
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
