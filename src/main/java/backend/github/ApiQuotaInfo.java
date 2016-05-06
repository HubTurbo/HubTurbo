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
    private final long millisecsToNextTopup;

    public ApiQuotaInfo(int remainingQuota, long millisecsToNextTopup) {
        this.remainingQuota = remainingQuota;
        this.millisecsToNextTopup = millisecsToNextTopup;
    }

    public int getRemainingQuota() {
        return remainingQuota;
    }

    public long minutesToNextQuotaTopup(){
        return Utility.minutesFromNow(millisecsToNextTopup);
    }
}
