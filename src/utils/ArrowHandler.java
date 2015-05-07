package utils;

import model.ActiveObject;
import model.ThreadEvent;
import supportModel.Arrow;
import views.ThreadFlowPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 4/28/15.
 */
public class ArrowHandler {
    private static ArrowHandler arrowHolder;
    private List<Arrow> arrows = new ArrayList<>();

    public static ArrowHandler instance() {
        if (arrowHolder == null){
            arrowHolder = new ArrowHandler();
        }
        return arrowHolder;
    }

    public List<Arrow> getArrows() {
        return arrows;
    }
    public void clearAll(){
        arrows.clear();
    }
    private boolean removeArrowsIfAdded(ThreadEvent threadEvent){
        List<Arrow> elementsToRemove = new ArrayList<>();
        for (Arrow arrow: arrows){
            if (arrow.getDestinationThreadEvent() == threadEvent)
                elementsToRemove.add(arrow);
            else if (arrow.getSourceThreadEvent() == threadEvent)
                elementsToRemove.add(arrow);
        }
        arrows.removeAll(elementsToRemove);
        return elementsToRemove.size() > 0;
    }
    private Arrow createArrow(ThreadEvent sourceThreadEvent, ThreadEvent destinationThreadEvent, ThreadFlowPanel sourcePanel, ThreadFlowPanel destinationPanel){
        for (Arrow arrow: arrows){
            if (arrow.getDestinationThreadEvent() == destinationThreadEvent)
                return null;
        }
        int y1 = 0;
        if (sourcePanel != null){
            y1 = sourcePanel.getY() + sourcePanel.getHeight() / 2;
        }
        int y2 = destinationPanel.getY() + destinationPanel.getHeight() / 2;
        return new Arrow(y1, y2, sourceThreadEvent, destinationThreadEvent);
    }
    private Arrow createArrowForThreadEvent(ThreadEvent sourceThreadEvent, ThreadEvent destinationThreadEvent, List<ThreadFlowPanel> flowPanels) {
        ThreadFlowPanel sourcePanel = null, destinationPanel = null;
        for (ThreadFlowPanel threadFlowPanel : flowPanels) {
            if (threadFlowPanel.getActiveObjectThread() == destinationThreadEvent.getThread()) {
                destinationPanel = threadFlowPanel;
            }
            if (threadFlowPanel.getActiveObjectThread().getThreadId() == destinationThreadEvent.getSenderThreadId()) {
                sourcePanel = threadFlowPanel;
                if (sourceThreadEvent == null) {
                    sourceThreadEvent = getSourceEvent(destinationThreadEvent, sourcePanel);
                }
            }
        }
        return createArrow(sourceThreadEvent, destinationThreadEvent, sourcePanel, destinationPanel);
    }
    public List<ActiveObject> addArrowsForEvent(ThreadEvent threadEvent, List<ThreadFlowPanel> flowPanels, DataHelper dataHelper) {
        List<Arrow> tempArrows = new ArrayList<>();
        if (!ArrowHandler.instance().removeArrowsIfAdded(threadEvent)) {
            addArrow(tempArrows, createArrowForThreadEvent(null, threadEvent, flowPanels));
            List<ThreadEvent> threadEvents = dataHelper.getOutgoingThreadEvents(threadEvent);
            for (ThreadEvent threadEvent1 : threadEvents) {
                addArrow(tempArrows, createArrowForThreadEvent(threadEvent, threadEvent1, flowPanels));
            }
        }
        this.arrows.addAll(tempArrows);
        return getAllActiveObjectsFromArrows(tempArrows);
    }
    private void addArrow(List<Arrow> arrows1, Arrow arrow){
        if (arrow != null)
            arrows1.add(arrow);
    }
    private ThreadEvent getSourceEvent(ThreadEvent threadEvent, ThreadFlowPanel flowPanel) {
        for (ThreadEvent tempThreadEvent : flowPanel.getActiveObjectThread().getEvents()) {
            if (threadEvent.getRequestSentTime() >= tempThreadEvent.getStartTime() && threadEvent.getRequestSentTime() <= tempThreadEvent.getFinishTime())
                return tempThreadEvent;
        }
        return null;
    }
    public Point updateArrows(List<ThreadFlowPanel> flowPanels){
        long timeExecuted = Long.MAX_VALUE;
        int yPosition = Integer.MAX_VALUE;
        for (Arrow arrow:arrows){
            updateArrow(arrow, flowPanels);
            System.out.println(arrow.getSourceThreadEvent() + " " + arrow.getDestinationThreadEvent());
            if (arrow.getDestinationThreadEvent().getRequestSentTime() < timeExecuted){
                timeExecuted = arrow.getDestinationThreadEvent().getRequestSentTime();
//                System.out.println("time executed=" + timeExecuted);
                yPosition = Math.min(arrow.getY1(), arrow.getY2());
            }
        }

        return new Point(SizeHelper.instance().convertTimeToLength(timeExecuted), yPosition);
    }
    private void updateArrow(Arrow arrow, List<ThreadFlowPanel> flowPanels){
        ThreadFlowPanel sourcePanel = null, destinationPanel = null;
        for (ThreadFlowPanel threadFlowPanel : flowPanels) {
            if (threadFlowPanel.getActiveObjectThread() == arrow.getDestinationThreadEvent().getThread()) {
                destinationPanel = threadFlowPanel;
            }
            if (threadFlowPanel.getActiveObjectThread().getThreadId() == arrow.getDestinationThreadEvent().getSenderThreadId()) {
                sourcePanel = threadFlowPanel;
            }
        }
        int y1 = 0;
        if (sourcePanel != null){
            y1 = sourcePanel.getY() + sourcePanel.getHeight() / 2;
        }
        int y2 = destinationPanel.getY() + destinationPanel.getHeight() / 2;
        arrow.setY1(y1);
        arrow.setY2(y2);
    }
    private List<ActiveObject> getAllActiveObjectsFromArrows(List<Arrow> arrowList){
        List<ActiveObject> activeObjects = new ArrayList<>();
        for (Arrow arrow:arrowList){
            ActiveObject sourceActiveObject = null;
            if (arrow.getSourceThreadEvent()!= null)
                sourceActiveObject = arrow.getSourceThreadEvent().getThread().getActiveObject();
            ActiveObject destinationActiveObject = arrow.getDestinationThreadEvent().getThread().getActiveObject();
            if (!activeObjects.contains(sourceActiveObject)){
                activeObjects.add(sourceActiveObject);
            }
            if (!activeObjects.contains(destinationActiveObject)){
                activeObjects.add(destinationActiveObject);
            }
        }

        return activeObjects;
    }
}
