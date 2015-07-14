package util.events;

public class UpdateRateLimitsEvent extends Event {
    public final int remainingRequests;
    public final long nextRefreshInMillisecs; // Epoch milliseconds

    public UpdateRateLimitsEvent(int remainingRequests, long nextRefreshInMillisecs) {
        this.remainingRequests = remainingRequests;
        this.nextRefreshInMillisecs = nextRefreshInMillisecs;
    }
}
