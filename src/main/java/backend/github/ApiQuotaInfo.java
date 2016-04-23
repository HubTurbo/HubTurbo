package backend.github;

import util.Utility;

/**
 * Holds GitHub API quota information. Data are immutable.
 */
public class ApiQuotaInfo {

    /**
     * The number of API requests remaining in the current rate limit window.
     */
    private final int remainingRequests;

    /**
     * The time at which the current API quota window resets in UTC epoch milliseconds.
     */
    private final long nextRefreshInMillisecs;

    public ApiQuotaInfo(int remainingRequests, long nextRefreshInMillisecs) {
        this.remainingRequests = remainingRequests;
        this.nextRefreshInMillisecs = nextRefreshInMillisecs;
    }

    public int getRemainingRequests() {
        return remainingRequests;
    }

    public long getNextRefreshInMillisecs() {
        return nextRefreshInMillisecs;
    }

    public long getNextRefreshInMinutesFromNow(){
        return Utility.minutesFromNow(nextRefreshInMillisecs);
    }
}
