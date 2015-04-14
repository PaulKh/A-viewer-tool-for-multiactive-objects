package supportModel;

import model.ThreadEvent;

/**
 * Created by pkhvoros on 4/8/15.
 */
public class Arrow {
    private int y1, y2;
    private ThreadEvent sourceThreadEvent;
    private ThreadEvent destinationThreadEvent;

    public Arrow(int y1, int y2, ThreadEvent sourceThreadEvent, ThreadEvent destinationThreadEvent) {
        this.y1 = y1;
        this.y2 = y2;
        this.sourceThreadEvent = sourceThreadEvent;
        this.destinationThreadEvent = destinationThreadEvent;
    }

    public ThreadEvent getSourceThreadEvent() {
        return sourceThreadEvent;
    }

    public ThreadEvent getDestinationThreadEvent() {
        return destinationThreadEvent;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }

}
