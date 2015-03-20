package views;

import utils.SizeHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Created by pkhvoros on 3/19/15.
 */
public class ThreadFlowPanel extends JPanel {
    private SizeHelper sizeHelper;
    public ThreadFlowPanel(SizeHelper sizeHelper) {
        updateSize(sizeHelper);
        this.setBackground(Color.CYAN);
    }
    public void updateSize(SizeHelper sizeHelper){
        this.sizeHelper = sizeHelper;
        setSize(sizeHelper.getLength(), SizeHelper.threadHeight);
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(sizeHelper.getLength(), SizeHelper.threadHeight);
    }
    //    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        this.setBackground(new Color(10, 25, 150));
//    }
}
