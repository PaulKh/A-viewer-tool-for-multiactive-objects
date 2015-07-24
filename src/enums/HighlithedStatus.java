package enums;

import java.awt.*;

/**
 * Created by pkhvoros on 7/20/15.
 */
public enum HighlithedStatus {
    NONE(null),
    DEPENDENCY_HIGHLIGHTED(Color.orange),
    COMPATIBILY_HIGHLITED(Color.green),
    BOTH_HIGHLIGHTED(Color.MAGENTA);
    private Color color;

    HighlithedStatus(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
