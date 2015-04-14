package views;

import model.ThreadEvent;
import supportModel.Arrow;
import utils.SizeHelper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 4/1/15.
 */
public class ScrollRootPanel extends JPanel {
    private List<Arrow> arrows = new ArrayList<>();
    private int flowX;

    public boolean removeArrowsIfAdded(ThreadEvent threadEvent){
        List<Arrow> elementsToRemove = new ArrayList<>();
        for (Arrow arrow:arrows){
            if (arrow.getDestinationThreadEvent() == threadEvent)
                elementsToRemove.add(arrow);
            else if (arrow.getSourceThreadEvent() == threadEvent)
                elementsToRemove.add(arrow);
        }
        arrows.removeAll(elementsToRemove);
        return elementsToRemove.size() > 0;
    }
    public void addArrow(ThreadEvent sourceThreadEvent, ThreadEvent destinationThreadEvent, ThreadFlowPanel sourcePanel, ThreadFlowPanel destinationPanel){
        for (Arrow arrow:arrows){
            if (arrow.getDestinationThreadEvent() == destinationThreadEvent)
                return;
        }
        int y1 = 0;
        if (sourcePanel != null){
            y1 = sourcePanel.getY() + sourcePanel.getHeight() / 2;
        }
        int y2 = destinationPanel.getY() + destinationPanel.getHeight() / 2;
        this.arrows.add(new Arrow(y1, y2, sourceThreadEvent, destinationThreadEvent));
    }

    public ScrollRootPanel(LayoutManager layout) {
        super(layout);
    }

    @Override
    public void paint(Graphics g1) {
        super.paint(g1);
        for (Arrow arrow: arrows){
            int x1 = SizeHelper.instance().convertTimeToLength(arrow.getDestinationThreadEvent().getRequestSentTime()) + flowX;
            int x2 = SizeHelper.instance().convertTimeToLength(arrow.getDestinationThreadEvent().getDerivedTime()) + flowX;
            drawArrowLine(g1, x1, arrow.getY1(), x2, arrow.getY2(), 6, 6);
        }
    }

    private void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h){
        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx*dx + dy*dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy/D, cos = dx/D;

        x = xm*cos - ym*sin + x1;
        ym = xm*sin + ym*cos + y1;
        xm = x;

        x = xn*cos - yn*sin + x1;
        yn = xn*sin + yn*cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(xpoints, ypoints, 3);
    }

    public List<Arrow> getArrows() {
        return arrows;
    }

    public void setFlowX(int flowX) {
        this.flowX = flowX;
    }
}