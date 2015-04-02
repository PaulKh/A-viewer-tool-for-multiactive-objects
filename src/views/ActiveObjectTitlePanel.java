package views;

import utils.SizeHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Created by pkhvoros on 3/20/15.
 */
public class ActiveObjectTitlePanel extends JPanel {
    private String title;

    public ActiveObjectTitlePanel(String title) {
        this.title = title;
        GridBagLayout gridBagLayout = new GridBagLayout();
        this.setLayout(gridBagLayout);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = constraints.weighty = 1.0;

        JTextArea label = new JTextArea("Active Object:" + title);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setBackground(null);
        gridBagLayout.setConstraints(label, constraints);
        this.add(label);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SizeHelper.activeObjectTitleWidth, 0);
    }

//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        g.setColor(Color.black);
//        g.fillRect(0, 0 , 100, 100);
//    }
}
