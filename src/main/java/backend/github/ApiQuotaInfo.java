package backend.github;

import util.Utility;

/**
 * Holds GitHub API quota information. Data are immutable.
 */
public class ApiQuotaInfo {

    /**
     * The remaining API quota in the current window.
     */
    private final int remainingQuota;

    /**
     * The time at which the current API quota window resets in UTC epoch milliseconds.
     */
    private final long nextRefreshInMillisecs;

    public ApiQuotaInfo(int remainingQuota, long nextRefreshInMillisecs) {
        this.remainingQuota = remainingQuota;
        this.nextRefreshInMillisecs = nextRefreshInMillisecs;
    }

    public int getRemainingQuota() {
        return remainingQuota;
    }

    public long getNextRefreshInMinutesFromNow(){
        return Utility.minutesFromNow(nextRefreshInMillisecs);
    }
}
