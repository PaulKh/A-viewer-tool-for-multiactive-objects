package supportModel;

import model.ActiveObject;
import model.ThreadEvent;

/**
 * Created by pkhvoros on 6/16/15.
 */
public abstract class Arrow {
    private ThreadEvent sourceThreadEvent;
    //Destination Thread event might be null

    public Arrow(ThreadEvent sourceThreadEvent) {
        this.sourceThreadEvent = sourceThreadEvent;
    }

    public ThreadEvent getSourceThreadEvent() {
        return sourceThreadEvent;
    }
    public abstract long getSentTime();
    public abstract long getDeliveredTime();
    public abstract ActiveObject getDestinationActiveObject();
}
