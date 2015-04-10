package supportModel;

import model.ThreadEvent;

/**
 * Created by pkhvoros on 4/8/15.
 */
public class Arrow {
    private long time1,time2;
    private int y1, y2;
    private ThreadEvent threadEvent;

    public Arrow(long x1, int y1, long x2, int y2, ThreadEvent threadEvent) {
        this.time1 = x1;
        this.y1 = y1;
        this.time2 = x2;
        this.y2 = y2;
        this.threadEvent = threadEvent;
    }

    public long getTime1() {
        return time1;
    }

    public int getY1() {
        return y1;
    }

    public long getTime2() {
        return time2;
    }

    public int getY2() {
        return y2;
    }

    public ThreadEvent getThreadEvent() {
        return threadEvent;
    }
}
