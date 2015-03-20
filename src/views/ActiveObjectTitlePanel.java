package views;

import javax.swing.*;
import java.awt.*;

/**
 * Created by pkhvoros on 3/20/15.
 */
public class ActiveObjectTitlePanel extends JPanel{
    private String title;

    public ActiveObjectTitlePanel(String title) {
        this.title = title;
        GridBagLayout gridBagLayout = new GridBagLayout();
        this.setLayout(gridBagLayout);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = constraints.weighty = 1.0;

        this.setBackground(new Color(100, 25, 150));
        JTextArea label = new JTextArea("Active Object:" + title);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        gridBagLayout.setConstraints(label, constraints);
        this.add(label);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, 100);
    }

//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        g.setColor(Color.black);
//        g.fillRect(0, 0 , 100, 100);
//    }
}
