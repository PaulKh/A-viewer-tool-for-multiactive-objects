package supportModel;

import enums.HighlithedStatus;
import model.ThreadEvent;

import java.awt.*;

/**
 * Created by pkhvoros on 5/21/15.
 */
public class RectangleWithThreadEvent {
    private HighlithedStatus highlithedStatus = HighlithedStatus.NONE;
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

    public HighlithedStatus getHighlithedStatus() {
        return highlithedStatus;
    }

    public void setDependencyHighlightedStatus(boolean highlighted) {
        if (highlighted) {
            if (highlithedStatus == HighlithedStatus.COMPATIBILY_HIGHLITED){
                highlithedStatus = HighlithedStatus.BOTH_HIGHLIGHTED;
            }
            else if (highlithedStatus == HighlithedStatus.NONE){
                highlithedStatus = HighlithedStatus.DEPENDENCY_HIGHLIGHTED;
            }
        }
        else {
            if (highlithedStatus == HighlithedStatus.BOTH_HIGHLIGHTED){
                highlithedStatus = HighlithedStatus.COMPATIBILY_HIGHLITED;
            }
            else if (highlithedStatus == HighlithedStatus.DEPENDENCY_HIGHLIGHTED){
                highlithedStatus = HighlithedStatus.NONE;
            }
        }
    }
    public void setCompatibilityHighlightedStatus(boolean highlighted) {
        if (highlighted) {
            if (highlithedStatus == HighlithedStatus.DEPENDENCY_HIGHLIGHTED){
                highlithedStatus = HighlithedStatus.BOTH_HIGHLIGHTED;
            }
            else if (highlithedStatus == HighlithedStatus.NONE){
                highlithedStatus = HighlithedStatus.COMPATIBILY_HIGHLITED;
            }
        }
        else {
            if (highlithedStatus == HighlithedStatus.BOTH_HIGHLIGHTED){
                highlithedStatus = HighlithedStatus.DEPENDENCY_HIGHLIGHTED;
            }
            else if (highlithedStatus == HighlithedStatus.COMPATIBILY_HIGHLITED){
                highlithedStatus = HighlithedStatus.NONE;
            }
        }
    }
}

