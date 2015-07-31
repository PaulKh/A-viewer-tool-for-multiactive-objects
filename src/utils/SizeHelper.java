package utils;

/**
 * Created by pkhvoros on 3/19/15.
 */
public class SizeHelper {
    public static int activeObjectTitleHeight = 100;
    public static int activeObjectTitleWidth = 160;
    public static int threadTitleWidth = 120;
    public static int threadHeight = 30;
    private static SizeHelper sizeHelper;
    private long minimumTime;
    private long maximumTime;
    private int scale;
    private int length;

    public static SizeHelper instance() {
        if (sizeHelper == null) {
            sizeHelper = new SizeHelper();
        }
        return sizeHelper;
    }

    public void setMaxMinScale(long maxValue, long minValue, int scale) {
        this.maximumTime = maxValue + 50;
        this.minimumTime = minValue;
        setScale(scale);
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

    public void setScale(int scale) {
        this.scale = scale;
        length = (int) ((maximumTime - minimumTime) / 1000) * scale;
    }

    public int getLength() {
        return length;
    }

    public int convertTimeToLength(long timeInMilliseconds) {
        long tempTime = timeInMilliseconds - minimumTime;
        return (int) ((length * tempTime) / (maximumTime - minimumTime));
    }

    public long convertLengthToTime(int xPosition) {
        return minimumTime + (xPosition * (maximumTime - minimumTime)) / length;
    }

    public int getTotalLength() {
        //third item in summation is padding on the right side
        return length + getLeftPadding() + 50;
    }

    public int getLeftPadding() {
        return activeObjectTitleWidth + threadTitleWidth;
    }

    public boolean didEventHappendBetweenMinAndMax(long time) {
        return time >= minimumTime && time <= maximumTime;
    }

}
