package views;

import utils.SizeHelper;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by pkhvoros on 3/23/15.
 */
public class ScalePanel extends JPanel {
    private SizeHelper sizeHelper;

    public ScalePanel(SizeHelper sizeHelper) {
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);
        this.sizeHelper = sizeHelper;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        List<TuplePositionTime> horizontalPoints = new ArrayList<>();
        long currentTime = sizeHelper.getMinimumTime() / 1000;//in seconds
        while (currentTime < sizeHelper.getMaximumTime() / 1000) {
            int length = sizeHelper.convertTimeToLength(currentTime * 1000);
            length = length < 0 ? 0 : length;
            horizontalPoints.add(new TuplePositionTime(length, currentTime * 1000));
            currentTime++;
        }
        horizontalPoints.add(new TuplePositionTime(sizeHelper.getLength(), sizeHelper.getMaximumTime()));

        for (TuplePositionTime tuplePositionTime : horizontalPoints) {
            g.fillRect(SizeHelper.threadTitleWidth + tuplePositionTime.getPosition(), 0, 2, 8);
        }
        g.fillRect(SizeHelper.threadTitleWidth, 8, sizeHelper.getLength(), 2);
        Date startDate = new Date(sizeHelper.getMinimumTime());
        g.getFontMetrics().stringWidth(dateFormat.format(startDate));
        removeAll();
        int lastLabelPosition = Integer.MIN_VALUE;
        for (TuplePositionTime tuplePositionTime : horizontalPoints) {
            int labelLength = g.getFontMetrics().stringWidth(dateFormat.format(startDate));
            if (SizeHelper.threadTitleWidth + tuplePositionTime.getPosition() - (labelLength / 2) < lastLabelPosition) {
                continue;
            }
            JLabel label = new JLabel(dateFormat.format(tuplePositionTime.getTime()));
            lastLabelPosition = SizeHelper.threadTitleWidth + tuplePositionTime.getPosition() + (labelLength / 2);
            label.setBounds(SizeHelper.threadTitleWidth + tuplePositionTime.getPosition() - (labelLength / 2), 12, 70, 15);
            add(label);
        }
    }

    public void updateView(SizeHelper sizeHelper) {
        this.sizeHelper = sizeHelper;
        this.setSize(sizeHelper.getLength(), 30);
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(sizeHelper.getLength(), 30);
    }

    private class TuplePositionTime {
        private int position;
        private long time;

        public TuplePositionTime(int position, long time) {
            this.position = position;
            this.time = time;
        }

        public int getPosition() {
            return position;
        }

        public long getTime() {
            return time;
        }
    }
}
