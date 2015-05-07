package views.builders;

import enums.IssueType;
import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.ErrorEntity;
import utils.SizeHelper;
import views.renderers.QueueDialogRenderer;
import views.table_models.DeliveryQueueTableModel;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by Paul on 30/04/15.
 */
public class QueuesDialogBuilder {
    public static Dialog buildQueueDialog(Frame owner, ActiveObject activeObject, long timePressed) {

        JDialog dialog = new JDialog(owner);
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

    private static List<ThreadEvent> getDeliveredList(ActiveObject activeObject, long timePressed){
        List<ThreadEvent> allThreadEventsToSort = new ArrayList<>();
        for (ActiveObjectThread thread:activeObject.getThreads()){
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
