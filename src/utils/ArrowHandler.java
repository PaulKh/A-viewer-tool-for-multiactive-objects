package utils;

import model.ActiveObject;
import model.ThreadEvent;
import supportModel.Arrow;
import supportModel.ArrowWithPosition;
import supportModel.CompleteArrow;
import views.FlowPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 4/28/15.
 */
public class ArrowHandler {
    private static ArrowHandler arrowHolder;
    private List<ArrowWithPosition> arrows = new ArrayList<>();

    public static ArrowHandler instance() {
        if (arrowHolder == null) {
            arrowHolder = new ArrowHandler();
        }
        return arrowHolder;
    }

    public List<ArrowWithPosition> getArrows() {
        return arrows;
    }

    public void clearAll() {
        arrows.clear();
    }

    private boolean removeArrowsIfAdded(ThreadEvent threadEvent) {
        List<ArrowWithPosition> elementsToRemove = new ArrayList<>();
        for (ArrowWithPosition arrow : arrows) {
            if (arrow.getArrow() instanceof CompleteArrow) {
                if (((CompleteArrow) arrow.getArrow()).getDestinationThreadEvent() == threadEvent)
                    elementsToRemove.add(arrow);
            }
            if (arrow.getArrow().getSourceThreadEvent() == threadEvent)
                elementsToRemove.add(arrow);
        }
        arrows.removeAll(elementsToRemove);
        return elementsToRemove.size() > 0;
    }

    private ArrowWithPosition enrichArrowWithYCoord(Arrow arrow, FlowPanel sourcePanel, FlowPanel destinationPanel) {
        int y1 = 0;
        if (sourcePanel != null) {
            y1 = sourcePanel.getY() + sourcePanel.getHeight() / 2;
        }
        int y2 = destinationPanel.getY() + destinationPanel.getHeight() / 2;
        if (SizeHelper.instance().didEventHappendBetweenMinAndMax(arrow.getSentTime()) &&
                SizeHelper.instance().didEventHappendBetweenMinAndMax(arrow.getDeliveredTime())) {
            return new ArrowWithPosition(y1, y2, arrow);
        }
        return null;
    }

    private ArrowWithPosition detectFlowsAndEnrichArrows(Arrow arrow, List<FlowPanel> flowPanels) {
        FlowPanel sourcePanel = null, destinationPanel = null;
        for (FlowPanel flowPanel : flowPanels) {
            if (arrow instanceof CompleteArrow) {
                if (flowPanel.containsThread(((CompleteArrow) arrow).getDestinationThreadEvent().getThread())) {
                    destinationPanel = flowPanel;
                }
            } else {
                if (flowPanel.getActiveObject().getIdentifier().equals(arrow.getDestinationActiveObject().getIdentifier())) {
                    destinationPanel = flowPanel;
                }
            }
            if (flowPanel.containsThread(arrow.getSourceThreadEvent().getThread())) {
                sourcePanel = flowPanel;
            }
        }
        return enrichArrowWithYCoord(arrow, sourcePanel, destinationPanel);
    }

    public List<ArrowWithPosition> addArrowsForEvent(ThreadEvent threadEvent, List<FlowPanel> flowPanels) {
        List<ArrowWithPosition> tempArrows = new ArrayList<>();
        if (!ArrowHandler.instance().removeArrowsIfAdded(threadEvent)) {
            for (Arrow arrow : threadEvent.getArrows()) {
                addArrow(tempArrows, detectFlowsAndEnrichArrows(arrow, flowPanels));
            }
        }
        this.arrows.addAll(tempArrows);
        return tempArrows;
    }

    private void addArrow(List<ArrowWithPosition> arrows1, ArrowWithPosition arrow) {
        if (arrow != null)
            arrows1.add(arrow);
    }

    private ThreadEvent getSourceEvent(ThreadEvent threadEvent, FlowPanel flowPanel) {
        for (ThreadEvent tempThreadEvent : flowPanel.getAllThreadEvents()) {
            if (threadEvent.getRequestSentTime() >= tempThreadEvent.getStartTime() && threadEvent.getRequestSentTime() <= tempThreadEvent.getFinishTime())
                return tempThreadEvent;
        }
        return null;
    }

    public void updateArrows(List<FlowPanel> flowPanels) {
        for (ArrowWithPosition arrow : arrows) {
            updateArrow(arrow, flowPanels);
        }
    }

    public Point getMostLeftAndTopPositionForArrows(List<ArrowWithPosition> arrows) {
        long timeExecuted = Long.MAX_VALUE;
        int yPosition = Integer.MAX_VALUE;
        for (ArrowWithPosition arrow : arrows) {
            if (arrow.getArrow().getSentTime() < timeExecuted) {
                timeExecuted = arrow.getArrow().getSentTime();
                yPosition = Math.min(arrow.getY1(), arrow.getY2());
            }
        }
        return new Point(SizeHelper.instance().convertTimeToLength(timeExecuted), yPosition);
    }

    private void updateArrow(ArrowWithPosition arrow, List<FlowPanel> flowPanels) {
        FlowPanel sourcePanel = null, destinationPanel = null;
        for (FlowPanel flowPanel : flowPanels) {
            if (arrow.getArrow() instanceof CompleteArrow) {
                if (flowPanel.containsThread(((CompleteArrow) arrow.getArrow()).getDestinationThreadEvent().getThread())) {
                    destinationPanel = flowPanel;
                }
            } else {
                if (flowPanel.getActiveObject().getIdentifier().equals(arrow.getArrow().getDestinationActiveObject().getIdentifier())) {
                    destinationPanel = flowPanel;
                }
            }
            if (flowPanel.containsThread(arrow.getArrow().getSourceThreadEvent().getThread())) {
                sourcePanel = flowPanel;
            }
        }
        int y1 = 0;
        if (sourcePanel != null) {
            y1 = sourcePanel.getY() + sourcePanel.getHeight() / 2;
        }
        int y2 = destinationPanel.getY() + destinationPanel.getHeight() / 2;
        arrow.setY1(y1);
        arrow.setY2(y2);
    }

    public List<ActiveObject> getAllActiveObjectsFromArrows(List<ArrowWithPosition> arrowList) {
        List<ActiveObject> activeObjects = new ArrayList<>();
        for (ArrowWithPosition arrow : arrowList) {
            ActiveObject sourceActiveObject = null;
            if (arrow.getArrow().getSourceThreadEvent() != null)
                sourceActiveObject = arrow.getArrow().getSourceThreadEvent().getThread().getActiveObject();
            if (!activeObjects.contains(sourceActiveObject)) {
                activeObjects.add(sourceActiveObject);
            }
            ActiveObject destinationActiveObject = arrow.getArrow().getDestinationActiveObject();
            if (!activeObjects.contains(destinationActiveObject)) {
                activeObjects.add(destinationActiveObject);
            }
        }

        return activeObjects;
    }
}
