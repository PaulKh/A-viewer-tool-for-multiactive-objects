package supportModel;

import model.ActiveObject;
import model.ThreadEvent;

/**
 * Created by pkhvoros on 6/18/15.
 */
public class CompleteArrow extends Arrow {
    private ThreadEvent destinationThreadEvent;

    public CompleteArrow(ThreadEvent sourceThreadEvent, ThreadEvent destinationThreadEvent) {
        super(sourceThreadEvent);
        this.destinationThreadEvent = destinationThreadEvent;
    }

    public ThreadEvent getDestinationThreadEvent() {
        return destinationThreadEvent;
    }

    @Override
    public long getSentTime() {
        return destinationThreadEvent.getRequestSentTime();
    }

    @Override
    public long getDeliveredTime() {
        return destinationThreadEvent.getDerivedTime();
    }

    @Override
    public ActiveObject getDestinationActiveObject() {
        return destinationThreadEvent.getThread().getActiveObject();
    }
}
