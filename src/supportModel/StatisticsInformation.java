package supportModel;

import model.ThreadEvent;

/**
 * Created by pkhvoros on 7/28/15.
 */
public class StatisticsInformation {
    private int totalNumberOfRequests;
    private int totalNumberOfArrows;
    private int numberOfActiveObjects;
    private ThreadEvent longestExecutedRequest;
    private ThreadEvent shortestExecutedRequest;
    private ThreadEvent mostWaitedRequest;
    private ThreadEvent leastWaitedRequest;
    private long averageQueueTime;

    public ThreadEvent getLongestExecutedRequest() {
        return longestExecutedRequest;
    }

    public void setLongestExecutedRequest(ThreadEvent longestExecutedRequest) {
        this.longestExecutedRequest = longestExecutedRequest;
    }

    public ThreadEvent getShortestExecutedRequest() {
        return shortestExecutedRequest;
    }

    public void setShortestExecutedRequest(ThreadEvent shortestExecutedRequest) {
        this.shortestExecutedRequest = shortestExecutedRequest;
    }

    public ThreadEvent getMostWaitedRequest() {
        return mostWaitedRequest;
    }

    public void setMostWaitedRequest(ThreadEvent mostWaitedRequest) {
        this.mostWaitedRequest = mostWaitedRequest;
    }

    public ThreadEvent getLeastWaitedRequest() {
        return leastWaitedRequest;
    }

    public void setLeastWaitedRequest(ThreadEvent leastWaitedRequest) {
        this.leastWaitedRequest = leastWaitedRequest;
    }

    public int getTotalNumberOfRequests() {
        return totalNumberOfRequests;
    }

    public void setTotalNumberOfRequests(int totalNumberOfRequests) {
        this.totalNumberOfRequests = totalNumberOfRequests;
    }

    public int getNumberOfActiveObjects() {
        return numberOfActiveObjects;
    }

    public void setNumberOfActiveObjects(int numberOfActiveObjects) {
        this.numberOfActiveObjects = numberOfActiveObjects;
    }

    public long getAverageQueueTime() {
        return averageQueueTime;
    }

    public void setAverageQueueTime(long averageQueueTime) {
        this.averageQueueTime = averageQueueTime;
    }

    public int getTotalNumberOfArrows() {
        return totalNumberOfArrows;
    }

    public void setTotalNumberOfArrows(int totalNumberOfArrows) {
        this.totalNumberOfArrows = totalNumberOfArrows;
    }
}
