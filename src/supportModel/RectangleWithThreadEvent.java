package supportModel;

import model.ThreadEvent;

import java.awt.*;

/**
 * Created by pkhvoros on 5/21/15.
 */
public class RectangleWithThreadEvent {
    private boolean isHighlighted = false;
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

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }
}

