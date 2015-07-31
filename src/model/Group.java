package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 7/16/15.
 */
public class Group {
    private String name;
    private List<Group> compatibleGroups = new ArrayList<>();
    private List<String> methodNames = new ArrayList<>();

    public Group(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Group> getCompatibleGroups() {
        return compatibleGroups;
    }

    public List<String> getMethodNames() {
        return methodNames;
    }

    public void addCompatibleGroup(Group group) {
        this.compatibleGroups.add(group);
    }

    public void addMethodName(String name) {
        this.methodNames.add(name);
    }
}
