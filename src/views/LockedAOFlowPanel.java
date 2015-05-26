package views;

import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.RectangleWithThreadEvent;
import utils.SizeHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.*;
import java.util.List;

/**
 * Created by pkhvoros on 5/21/15.
 */
public class LockedAOFlowPanel extends FlowPanel implements MouseMotionListener {
    private ActiveObject activeObject;
    public LockedAOFlowPanel(ActiveObject activeObject) {
        this.activeObject = activeObject;
        for(ActiveObjectThread activeObjectThread:activeObject.getThreads())
            for (ThreadEvent threadEvent : activeObjectThread.getEvents())
                rectangles.add(new RectangleWithThreadEvent(threadEvent));
        this.addMouseMotionListener(this);
        initMouseClickListener();
        init();
    }
    private void initMouseClickListener(){
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                RectangleWithThreadEvent rect = getRectangleContainingPoint(e, 0);
                if (rect != null && callback != null) {
                    callback.threadEventClicked(rect.getThreadEvent());
                } else {
                    rect = getRectangleContainingPoint(e, 5);
                    if (rect != null && callback != null) {
                        callback.threadEventClicked(rect.getThreadEvent());
                    }
                }
                long time = SizeHelper.instance().convertLengthToTime(e.getX());
                if (callback != null) {
                    callback.threadClicked(activeObject, time);
                }
            }
        });
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (RectangleWithThreadEvent rect : rectangles) {
            g.setColor(new Color(128, 205, 255));
            g.fillRect(rect.getRectangle().x, rect.getRectangle().y, rect.getRectangle().width, rect.getRectangle().height);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        RectangleWithThreadEvent rect = getRectangleContainingPoint(e, 0);
        if (rect != null) {

        }
    }

    @Override
    public boolean containsThread(ActiveObjectThread thread) {
        for (ActiveObjectThread tempThread:activeObject.getThreads()){
            if (thread.equals(tempThread))
                return true;
        }
        return false;
    }
    @Override
    public boolean containsSourceThreadForEvent(ThreadEvent threadEvent) {
        for (ActiveObjectThread activeObjectThread: activeObject.getThreads())
            if (activeObjectThread.getThreadId() == threadEvent.getSenderThreadId() && activeObjectThread.getActiveObject().getIdentifier() == threadEvent.getSenderActiveObjectId())
                return true;
        return false;
    }

    @Override
    public List<ThreadEvent> getAllThreadEvents() {
        List<ThreadEvent> resultEvents = new ArrayList<>();
        for (ActiveObjectThread thread:activeObject.getThreads()){
            resultEvents.addAll(thread.getEvents());
        }
        return resultEvents;
    }

}
