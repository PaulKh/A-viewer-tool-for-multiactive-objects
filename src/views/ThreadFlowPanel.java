package views;

import callbacks.ThreadEventClickedCallback;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.Arrow;
import supportModel.RectangleWithThreadEvent;
import utils.ArrowHandler;
import utils.SizeHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 3/19/15.
 */
public class ThreadFlowPanel extends FlowPanel implements MouseMotionListener, MouseListener {

    private ActiveObjectThread activeObjectThread;
    private ThreadEventClickedCallback callback;
    public ThreadFlowPanel(ActiveObjectThread activeObjectThread) {
        this.activeObjectThread = activeObjectThread;
        for (ThreadEvent threadEvent : activeObjectThread.getEvents())
            rectangles.add(new RectangleWithThreadEvent(threadEvent));
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        init();
    }





    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int counter = 0;
        for (RectangleWithThreadEvent rect : rectangles) {
            counter++;
            if (rect.getThreadEvent().getFinishTime() <= 0) {
                g.setColor(new Color(255, 0, 0));
            } else if (counter % 2 == 0) {
                g.setColor(new Color(128, 205, 255));
            } else {
                g.setColor(new Color(255, 248, 57));
            }
            g.fillRect(rect.getRectangle().x, rect.getRectangle().y, rect.getRectangle().width, rect.getRectangle().height);
            if (rect.isHighlighted())
                drawHighlightedRectangle(g, rect.getRectangle());
//            else{
//               g.fillRect(rect.getRectangle().x, rect.getRectangle().y, sizeHelper.getLength() - rect.getRectangle().x, rect.getRectangle().height);
//            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        RectangleWithThreadEvent rect = getRectangleContainingPoint(e, 0);
//        System.out.print(e.getX());
        if (rect != null) {
            mouseMovedRectNotNull(rect);
        } else {
            rect = getRectangleContainingPoint(e, 5);
            if (rect != null) {
                mouseMovedRectNotNull(rect);
            } else {
                setToolTipText(null);
            }
        }
    }

    private void mouseMovedRectNotNull(RectangleWithThreadEvent rect) {
        if (rect.getThreadEvent().getFinishTime() <= 0) {
            setToolTipText("<html>ERROR:Request never stop<br>Caller:" + rect.getThreadEvent().getSenderActiveObjectId() + "<br>Method:" + rect.getThreadEvent().getUniqueMethodName() + "</html>");
        } else {
            float duration = (float) (((rect.getThreadEvent().getFinishTime() - rect.getThreadEvent().getStartTime()) / 10) / 100.0);
            setToolTipText("<html>Caller:" + rect.getThreadEvent().getSenderActiveObjectId() + "<br>Method:" + rect.getThreadEvent().getUniqueMethodName() + "<br>Duration:" + duration + " sec</html>");
        }
    }



    @Override
    public void mouseClicked(MouseEvent e) {
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
            callback.threadClicked(activeObjectThread.getActiveObject(), time);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }


    public ThreadEventClickedCallback getCallback() {
        return callback;
    }

    public void setCallback(ThreadEventClickedCallback callback) {
        this.callback = callback;
    }

    public ActiveObjectThread getActiveObjectThread() {
        return activeObjectThread;
    }

    private void drawHighlightedRectangle(Graphics g, Rectangle rect) {
        g.setColor(Color.ORANGE);
//        Graphics2D g2 = (Graphics2D) g;
//        float thickness = 20;
//        Stroke oldStroke = g2.getStroke();
//        g2.setStroke(new BasicStroke(thickness));
        int thickness = 8;
        for (int i = 0; i < thickness; i++) {
            g.setColor(new Color(255, 143, 20, (255 * (thickness - i)) / thickness));
            g.drawRect(rect.x + i, rect.y + i, rect.width - (i * 2), rect.height - (i * 2));
        }
//        g2.setStroke(oldStroke);

    }

    public void setHighlightedEvent() {
        for (RectangleWithThreadEvent rect : rectangles) {
            rect.setHighlighted(false);
        }
        for (Arrow arrow : ArrowHandler.instance().getArrows()) {
            for (RectangleWithThreadEvent rect : rectangles) {
                if (rect.getThreadEvent() == arrow.getSourceThreadEvent()) {
                    rect.setHighlighted(true);
                } else if (rect.getThreadEvent() == arrow.getDestinationThreadEvent()) {
                    rect.setHighlighted(true);
                }
            }
        }
    }

    @Override
    public boolean containsThread(ActiveObjectThread thread) {
        return thread.equals(activeObjectThread);
    }

    @Override
    public boolean containsSourceThreadForEvent(ThreadEvent threadEvent) {
        if (activeObjectThread.getThreadId() == threadEvent.getSenderThreadId() && activeObjectThread.getActiveObject().getIdentifier() == threadEvent.getSenderActiveObjectId())
            return true;
        return false;
    }

    @Override
    public List<ThreadEvent> getAllThreadEvents() {
        return this.activeObjectThread.getEvents();
    }
}
