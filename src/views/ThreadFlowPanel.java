package views;

import callbacks.ThreadEventClickedCallback;
import enums.HighlithedStatus;
import enums.MenuItemType;
import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.ArrowWithPosition;
import supportModel.CompleteArrow;
import supportModel.RectangleWithThreadEvent;
import utils.ArrowHandler;
import utils.CompatibilityHelper;
import utils.SizeHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 3/19/15.
 */
public class ThreadFlowPanel extends FlowPanel implements MouseMotionListener {

    private ActiveObjectThread activeObjectThread;

    public ThreadFlowPanel(ActiveObjectThread activeObjectThread) {
        this.activeObjectThread = activeObjectThread;
        for (ThreadEvent threadEvent : activeObjectThread.getEvents())
            rectangles.add(new RectangleWithThreadEvent(threadEvent));
        this.addMouseMotionListener(this);
        initMouseClickListener();
        init();
    }

    private void initMouseClickListener() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                long time = SizeHelper.instance().convertLengthToTime(e.getX());
                if (callback != null) {
                    callback.threadClicked(activeObjectThread.getActiveObject(), time);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    popContextMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    popContextMenu(e);
            }
        });
    }

    private void popContextMenu(MouseEvent e) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem showDependenciesMenuItem = new JMenuItem("Show dependencies");
        JMenuItem showCompatibilityInfo = new JMenuItem("Show compatibility info");
        popupMenu.add(showDependenciesMenuItem);
        popupMenu.add(showCompatibilityInfo);

        showDependenciesMenuItem.addActionListener(new MenuActionListener(MenuItemType.DEPENDENCIES, e));
        showCompatibilityInfo.addActionListener(new MenuActionListener(MenuItemType.COMPATIBILITY, e));
        if (CompatibilityHelper.instance().getEventForKey(getActiveObject()) != null) {
            JMenuItem removeCompatibilityHighlight = new JMenuItem("Remove compatibility highlight");
            popupMenu.add(removeCompatibilityHighlight);
            removeCompatibilityHighlight.addActionListener(e1 -> {
                if (callback != null) {
                    callback.removeCompatibilityClicked(getActiveObject());
                }
            });
        }
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
//        showDependenciesMenuItem.addActionListener();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int counter = 0;
        for (RectangleWithThreadEvent rect : rectangles) {
            counter++;
            if (!rect.getThreadEvent().isEventLastsAnyTime()) {
                g.setColor(new Color(255, 0, 0));
            } else if (counter % 2 == 0) {
                g.setColor(new Color(128, 205, 255));
            } else {
                g.setColor(new Color(255, 248, 57));
            }
            g.fillRect(rect.getRectangle().x, rect.getRectangle().y, rect.getRectangle().width, rect.getRectangle().height);
            if (rect.getHighlithedStatus() != HighlithedStatus.NONE)
                drawHighlightedRectangle(g, rect);
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

    public ThreadEventClickedCallback getCallback() {
        return callback;
    }

    public void setCallback(ThreadEventClickedCallback callback) {
        this.callback = callback;
    }

    public ActiveObjectThread getActiveObjectThread() {
        return activeObjectThread;
    }

    private void drawHighlightedRectangle(Graphics g, RectangleWithThreadEvent rect) {
        Rectangle rectangle = rect.getRectangle();
//        g.setColor(rect.getHighlithedStatus().getColor());
//        Graphics2D g2 = (Graphics2D) g;
//        float thickness = 20;
//        Stroke oldStroke = g2.getStroke();
//        g2.setStroke(new BasicStroke(thickness));
        int thickness = 8;
        Color c = rect.getHighlithedStatus().getColor();
        for (int i = 0; i < thickness; i++) {
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (255 * (thickness - i)) / thickness));
            g.drawRect(rectangle.x + i, rectangle.y + i, rectangle.width - (i * 2), rectangle.height - (i * 2));
        }
//        g2.setStroke(oldStroke);

    }

    public void removeCompatibilityHighlight() {
        for (RectangleWithThreadEvent rectangleWithThreadEvent : rectangles) {
            rectangleWithThreadEvent.setCompatibilityHighlightedStatus(false);
        }
    }

    public void setHighlightedEvent() {
        for (RectangleWithThreadEvent rect : rectangles) {
            rect.setDependencyHighlightedStatus(false);
        }
        for (ArrowWithPosition arrow : ArrowHandler.instance().getArrows()) {
            for (RectangleWithThreadEvent rect : rectangles) {
                if (rect.getThreadEvent() == arrow.getArrow().getSourceThreadEvent()) {
                    rect.setDependencyHighlightedStatus(true);
                } else if (arrow.getArrow() instanceof CompleteArrow) {
                    if (rect.getThreadEvent() == ((CompleteArrow) arrow.getArrow()).getDestinationThreadEvent()) {
                        rect.setDependencyHighlightedStatus(true);
                    }
                }
            }
        }
    }

    public void highlightCompatibility(ThreadEvent threadEvent) {
        for (RectangleWithThreadEvent rectangleWithThreadEvent : rectangles) {
            rectangleWithThreadEvent.setCompatibilityHighlightedStatus(false);
        }
        ActiveObject activeObject = this.activeObjectThread.getActiveObject();
        for (RectangleWithThreadEvent rectangleWithThreadEvent : rectangles) {
            if (!threadEvent.equals(rectangleWithThreadEvent.getThreadEvent())) {
                if (activeObject.areEventsCompatible(threadEvent, rectangleWithThreadEvent.getThreadEvent())) {
                    rectangleWithThreadEvent.setCompatibilityHighlightedStatus(true);
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
        return new ArrayList<>(this.activeObjectThread.getEvents());
    }

    @Override
    public ActiveObject getActiveObject() {
        return activeObjectThread.getActiveObject();
    }

    private class MenuActionListener implements ActionListener {
        private MenuItemType menuItemType;
        private MouseEvent mouseEvent;

        public MenuActionListener(MenuItemType menuItemType, MouseEvent mouseEvent) {
            this.menuItemType = menuItemType;
            this.mouseEvent = mouseEvent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RectangleWithThreadEvent rect = getRectangleContainingPoint(mouseEvent, 0);
            if (rect != null && callback != null) {
                clickEvent(rect);
            } else {
                rect = getRectangleContainingPoint(mouseEvent, 5);
                if (rect != null && callback != null) {
                    clickEvent(rect);
                }
            }
        }

        private void clickEvent(RectangleWithThreadEvent rect) {
            callback.threadEventClicked(menuItemType, rect.getThreadEvent());
        }
    }
}
