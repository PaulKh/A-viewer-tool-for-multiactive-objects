package views;

import callbacks.ThreadEventClickedCallback;
import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.RectangleWithThreadEvent;
import utils.SizeHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 5/21/15.
 */
public abstract class FlowPanel extends JPanel {
    protected ThreadEventClickedCallback callback;
    protected List<RectangleWithThreadEvent> rectangles = new ArrayList<>();

    protected void init() {
        ToolTipManager.sharedInstance().setInitialDelay(0);
        this.setBackground(Color.WHITE);
        updateSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SizeHelper.instance().getTotalLength(), SizeHelper.threadHeight);
    }

    public void updateSize() {
        SizeHelper sizeHelper = SizeHelper.instance();
        long totalTime = sizeHelper.getMaximumTime() - sizeHelper.getMinimumTime();
        for (RectangleWithThreadEvent event : rectangles) {
            long startTime = event.getThreadEvent().getStartTime() - sizeHelper.getMinimumTime();
            long finishTime = event.getThreadEvent().getFinishTime() - sizeHelper.getMinimumTime();
            int xPos = (int) (startTime * sizeHelper.getLength() / totalTime);
            int length = (int) (finishTime * sizeHelper.getLength() / totalTime) - xPos;
            if (length == 0)
                length = 1;
            event.setRectangle(new Rectangle(xPos, 0, length, SizeHelper.threadHeight));
            if (finishTime <= 0)
                event.setRectangle(new Rectangle(xPos, 0, sizeHelper.getLength() - xPos, SizeHelper.threadHeight));
//                System.out.println("thread id = " + activeObjectThread.getThreadId() + " length = " + length + " finishTime = " + finishTime);
        }
        setSize(sizeHelper.getTotalLength(), SizeHelper.threadHeight);
    }

    protected RectangleWithThreadEvent getRectangleContainingPoint(MouseEvent mouseEvent, int delta) {
        int mx = mouseEvent.getX();
        int my = mouseEvent.getY();
        for (RectangleWithThreadEvent rect : rectangles) {
            Rectangle tempRect = new Rectangle((int) rect.getRectangle().getX() - delta, (int) rect.getRectangle().getY(), (int) rect.getRectangle().getWidth() + delta * 2, (int) rect.getRectangle().getHeight());
            if (tempRect.contains(mx, my)) {
                return rect;
            }
        }
        return null;
    }

    public abstract boolean containsThread(ActiveObjectThread thread);

    public abstract boolean containsSourceThreadForEvent(ThreadEvent threadEvent);

    public abstract List<ThreadEvent> getAllThreadEvents();

    public abstract ActiveObject getActiveObject();

    public void deHighlightAllTheRectangles() {
        for (RectangleWithThreadEvent rect : rectangles) {
            rect.setHighlighted(false);
        }
    }
}
