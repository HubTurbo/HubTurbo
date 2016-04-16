package util.events;

/**
 * An event that is called when new API rate limits information is available.
 */
public class RateLimitsUpdatedEvent extends Event{

    public final int remainingRequests;
    public final long nextRefreshInMillisecs; // Epoch milliseconds

    public RateLimitsUpdatedEvent(int remainingRequests, long nextRefreshInMillisecs) {
        this.remainingRequests = remainingRequests;
        this.nextRefreshInMillisecs = nextRefreshInMillisecs;
    }

}
