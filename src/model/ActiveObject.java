package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 3/13/15.
 */
public class ActiveObject {
    private String identifier;
    private List<ActiveObjectThread> threads = new ArrayList<ActiveObjectThread>();
    private List<Group> groups = new ArrayList<>();

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

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ActiveObject) {
            return ((ActiveObject) obj).getIdentifier().equals(identifier);
        }
        return false;
    }

    public boolean areEventsCompatible(ThreadEvent threadEvent1, ThreadEvent threadEvent2) {
        Group groupOfFirstEvent = null;
        for (Group tempGroup : groups) {
            if (tempGroup.getMethodNames().contains(threadEvent1.getMethodName())) {
                groupOfFirstEvent = tempGroup;
                break;
            }
        }
        if (groupOfFirstEvent == null)
            return false;
        for (Group tempGroup : groups) {
            if (tempGroup.getMethodNames().contains(threadEvent2.getMethodName())) {
                if (tempGroup.getCompatibleGroups().contains(groupOfFirstEvent)) {
                    return true;
                }
                break;
            }
        }
        return false;
    }
}
