package views;

import utils.SizeHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Created by pkhvoros on 3/20/15.
 */
public class ThreadTitlePanel extends JPanel {
    private String title;

    public ThreadTitlePanel(String title) {
        this.title = title;
        GridBagLayout gridBagLayout = new GridBagLayout();
        this.setLayout(gridBagLayout);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = constraints.weighty = 1.0;

        this.setBackground(new Color(100, 250, 150));
        JTextArea label = new JTextArea("Thread Id:" + title);
        label.setLineWrap(true);
        label.setBackground(null);
        label.setWrapStyleWord(true);
        label.setEditable(false);
        gridBagLayout.setConstraints(label, constraints);
        this.add(label);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SizeHelper.threadTitleWidth, SizeHelper.threadHeight);
    }

//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        g.setColor(Color.black);
//        g.fillRect(0, 0 , 100, 100);
//    }
}
