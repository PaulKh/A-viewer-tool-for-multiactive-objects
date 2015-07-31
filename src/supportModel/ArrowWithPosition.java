package supportModel;

/**
 * Created by pkhvoros on 4/8/15.
 */
public class ArrowWithPosition {
    private int y1, y2;
    private Arrow arrow;

    public ArrowWithPosition(int y1, int y2, Arrow arrow) {
        this.y1 = y1;
        this.y2 = y2;
        this.arrow = arrow;
    }

    public Arrow getArrow() {
        return arrow;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

}
