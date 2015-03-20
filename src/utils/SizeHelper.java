package utils;

/**
 * Created by pkhvoros on 3/19/15.
 */
public class SizeHelper {
    public static int threadHeight = 30;
    private static long minimumTime;
    private static long maximumTime;
    private int scale;
    private int length;

    public SizeHelper(long minimumTime, long maximumTime, int scale) {
        this.minimumTime = minimumTime;
        this.maximumTime = maximumTime;
        this.scale = scale;
        length = (int)(maximumTime - minimumTime)*scale/1000;
    }

    public SizeHelper(int scale) {
        this.scale = scale;
        length = (int)(maximumTime - minimumTime)*scale/1000;
    }

    public long getMinimumTime() {
        return minimumTime;
    }

    public long getMaximumTime() {
        return maximumTime;
    }

    public int getScale() {
        return scale;
    }

    public int getLength() {
        return length;
    }
}
