package views;

import supportModel.ArrowWithPosition;
import utils.ArrowHandler;
import utils.SizeHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Created by pkhvoros on 4/1/15.
 */
public class ScrollRootPanel extends JPanel {
    private int flowX;


    public ScrollRootPanel(LayoutManager layout) {
        super(layout);
    }

    @Override
    public void paint(Graphics g1) {
        super.paint(g1);
        for (ArrowWithPosition arrow : ArrowHandler.instance().getArrows()) {
            int x1 = SizeHelper.instance().convertTimeToLength(arrow.getArrow().getSentTime()) + flowX;
            int x2 = SizeHelper.instance().convertTimeToLength(arrow.getArrow().getDeliveredTime()) + flowX;
            if (!(x1 <= 0 || x1 >= SizeHelper.instance().getTotalLength() || x2 <= 0 || x2 >= SizeHelper.instance().getTotalLength()))
                drawArrowLine(g1, x1, arrow.getY1(), x2, arrow.getY2(), 6, 6);

        }
    }

    private void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx * dx + dy * dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;

        x = xm * cos - ym * sin + x1;
        ym = xm * sin + ym * cos + y1;
        xm = x;

        x = xn * cos - yn * sin + x1;
        yn = xn * sin + yn * cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(xpoints, ypoints, 3);
    }

    public void setFlowX(int flowX) {
        this.flowX = flowX;
    }
}
