package supportModel;

import model.ActiveObject;
import model.ThreadEvent;

/**
 * Created by pkhvoros on 6/18/15.
 */
public class NotCompleteArrow extends Arrow {
    private long sentTime;
    private long derivedTime;
    private ActiveObject receiverActiveObject;

    public NotCompleteArrow(ThreadEvent sourceThreadEvent, long sentTime, long derivedTime, ActiveObject receiverActiveObject) {
        super(sourceThreadEvent);
        this.derivedTime = derivedTime;
        this.sentTime = sentTime;
        this.receiverActiveObject = receiverActiveObject;
    }

    public long getSentTime() {
        return sentTime;
    }

    @Override
    public long getDeliveredTime() {
        return derivedTime;
    }

    @Override
    public ActiveObject getDestinationActiveObject() {
        return receiverActiveObject;
    }
}
