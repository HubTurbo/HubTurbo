package util.events;

/**
 * An event that is generated when new API rate limits information is available.
 */
public class NewApiQuotaInfoAvailableEvent extends Event{

    /**
     * The number of API requests remaining in the current rate limit window.
     */
    public final int remainingRequests;

    /**
     * The time at which the current API rate limit window resets in UTC epoch milliseconds.
     */
    public final long nextRefreshInMillisecs;

    public NewApiQuotaInfoAvailableEvent(int remainingRequests, long nextRefreshInMillisecs) {
        this.remainingRequests = remainingRequests;
        this.nextRefreshInMillisecs = nextRefreshInMillisecs;
    }

}
