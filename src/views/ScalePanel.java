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

    public ScalePanel() {
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        List<TuplePositionTime> horizontalPoints = new ArrayList<>();
        long currentTime = SizeHelper.instance().getMinimumTime() / 1000;//in seconds
        while (currentTime < SizeHelper.instance().getMaximumTime() / 1000) {
            int length = SizeHelper.instance().convertTimeToLength(currentTime * 1000);
            length = length < 0 ? 0 : length;
            horizontalPoints.add(new TuplePositionTime(length, currentTime * 1000));
            currentTime++;
        }
        horizontalPoints.add(new TuplePositionTime(SizeHelper.instance().getLength(), SizeHelper.instance().getMaximumTime()));

//        for (TuplePositionTime tuplePositionTime : horizontalPoints) {
//            g.fillRect(SizeHelper.threadTitleWidth + tuplePositionTime.getPosition(), 0, 2, 8);
//        }
        int padding = SizeHelper.instance().getLeftPadding();
        g.fillRect(padding, 8, SizeHelper.instance().getLength() + 1, 2);
        Date startDate = new Date(SizeHelper.instance().getMinimumTime());
        g.getFontMetrics().stringWidth(dateFormat.format(startDate));
        removeAll();
        int lastLabelPosition = Integer.MIN_VALUE;
        for (TuplePositionTime tuplePositionTime : horizontalPoints) {
            int labelLength = g.getFontMetrics().stringWidth(dateFormat.format(startDate));
            if (padding + tuplePositionTime.getPosition() - (labelLength / 2) < lastLabelPosition) {
                continue;
            }
            JLabel label = new JLabel(dateFormat.format(tuplePositionTime.getTime()));
            lastLabelPosition = padding + tuplePositionTime.getPosition() + (labelLength / 2) + 5;
            label.setBounds(padding + tuplePositionTime.getPosition() - (labelLength / 2), 12, 70, 15);
            g.fillRect(padding + tuplePositionTime.getPosition(), 0, 2, 8);
            add(label);
        }
    }

    public void updateView() {
        this.setSize(SizeHelper.instance().getTotalLength(), 30);
//        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SizeHelper.instance().getTotalLength(), 30);
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
