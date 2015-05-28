package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 3/13/15.
 */
public class ActiveObject {
    private String identifier;
    private List<ActiveObjectThread> threads = new ArrayList<ActiveObjectThread>();

    public ActiveObject(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
//
//
//    public void setIdentifier(String identifier) {
//        this.identifier = identifier;
//    }

    public ActiveObjectThread addThreadWithId(int id) {
        for (ActiveObjectThread thread : threads) {
            if (thread.getThreadId() == id)
                return thread;
        }
        ActiveObjectThread thread = new ActiveObjectThread(id, this);
        threads.add(thread);
        return thread;
    }

    public ActiveObjectThread getThreadWithId(int id) {
        for (ActiveObjectThread thread : threads)
            if (thread.getThreadId() == id)
                return thread;
        return null;
    }

    public List<ActiveObjectThread> getThreads() {
        return threads;
    }
}
