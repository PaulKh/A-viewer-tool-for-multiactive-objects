package supportModel;

import model.ThreadEvent;

import java.util.List;

/**
 * Created by pkhvoros on 7/24/15.
 */
public class WrappedQueueCompatibilityData {
    private List<ThreadEvent> threadEvents;
    private int compatibilityIdentifierSelected = -1;

    public WrappedQueueCompatibilityData(List<ThreadEvent> threadEvents) {
        this.threadEvents = threadEvents;
    }

    public int getCompatibilityIdentifierSelected() {
        return compatibilityIdentifierSelected;
    }

    public void setCompatibilityIdentifierSelected(int compatibilityIdentifierSelected) {
        this.compatibilityIdentifierSelected = compatibilityIdentifierSelected;
    }

    public List<ThreadEvent> getThreadEvents() {
        return threadEvents;
    }
}
