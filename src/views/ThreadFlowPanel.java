package views;

import callbacks.ThreadEventClickedCallback;
import model.ActiveObjectThread;
import model.ThreadEvent;
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
public class ThreadFlowPanel extends JPanel implements MouseMotionListener, MouseListener {
    private List<RectangleWithThreadEvent> rectangles = new ArrayList<>();
    private ActiveObjectThread activeObjectThread;
    private ThreadEventClickedCallback callback;

    public ThreadFlowPanel(ActiveObjectThread activeObjectThread) {
        this.activeObjectThread = activeObjectThread;
        ToolTipManager.sharedInstance().setInitialDelay(0);
        for (ThreadEvent threadEvent : activeObjectThread.getEvents())
            rectangles.add(new RectangleWithThreadEvent(threadEvent));
        this.setBackground(Color.WHITE);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        updateSize();
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
        setSize(sizeHelper.getLength(), SizeHelper.threadHeight);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SizeHelper.instance().getLength(), SizeHelper.threadHeight);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int counter = 0;
        for (RectangleWithThreadEvent rect : rectangles) {
            counter++;
            if (rect.getThreadEvent().getFinishTime() <= 0) {
                g.setColor(new Color(255, 0, 0));
            }
            else if (counter % 2 == 0){
                g.setColor(new Color(128, 205, 255));
            }
            else{
                g.setColor(new Color(255, 248, 57));
            }
            g.fillRect(rect.getRectangle().x, rect.getRectangle().y, rect.getRectangle().width, rect.getRectangle().height);
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
        RectangleWithThreadEvent rect = getRectangleContainingPoint(e);
        if (rect != null){
            if (rect.getThreadEvent().getFinishTime() <= 0){
                setToolTipText("<html>ERROR:Request never stop<br>Caller:" + rect.getThreadEvent().getSenderActiveObjectId() + "<br>Method:" + rect.getThreadEvent().getMethodName() + "</html>");
            }
            else {
                float duration = (float) (((rect.getThreadEvent().getFinishTime() - rect.getThreadEvent().getStartTime()) / 10) / 100.0);
                setToolTipText("<html>Caller:" + rect.getThreadEvent().getSenderActiveObjectId() + "<br>Method:" + rect.getThreadEvent().getMethodName() + "<br>Duration:" + duration + " sec</html>");
            }
        }
        else
            setToolTipText(null);
    }
    private RectangleWithThreadEvent getRectangleContainingPoint(MouseEvent mouseEvent){
        int mx = mouseEvent.getX();
        int my = mouseEvent.getY();
        for (RectangleWithThreadEvent rect : rectangles) {
            if (rect.getRectangle().contains(mx, my)) {
                return rect;
            }
        }
        return null;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        RectangleWithThreadEvent rect = getRectangleContainingPoint(e);
        if (rect != null && callback != null){
            callback.threadEventClicked(rect.threadEvent);
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

    private class RectangleWithThreadEvent {
        private Rectangle rectangle;
        private ThreadEvent threadEvent;

        public RectangleWithThreadEvent(ThreadEvent threadEvent) {
            this.threadEvent = threadEvent;
        }

        public Rectangle getRectangle() {
            return rectangle;
        }

        public void setRectangle(Rectangle rectangle) {
            this.rectangle = rectangle;
        }

        public ThreadEvent getThreadEvent() {
            return threadEvent;
        }
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
}
