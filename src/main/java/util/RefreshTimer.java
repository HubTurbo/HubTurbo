package util;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Facilitates the countdown for periodic refreshes.
 */
public class RefreshTimer extends TickingTimer{

    /**
     * The buffer time to be added to ensure refresh happens after the api quota renewal.
     */
    public static final int BUFFER_TIME = 1;

    /**
     * The amount that is set aside for manual operations by the user.
     */
    public static final int API_QUOTA_BUFFER = 200;

    /**
     * The default refresh period of the refresh rate.
     */
    public static final int DEFAULT_REFRESH_PERIOD_IN_MINS = 1;

    public RefreshTimer(String name, int period, Consumer<Integer> onTick, Runnable onTimeout,
                        TimeUnit timeUnit) {
        super(name, period, onTick, onTimeout, timeUnit);
    }

    /**
     * Computes the TickerTimer period that is used for refreshing the issues periodically
     * Assumes future refreshes will take the same number of API calls as the last refresh and find out the refresh
     * duration that will spread out the refreshes until the next api quota top up.
     *
     * For some cases where the refresh rate is equal to the remainingTimeInMins,
     * BUFFER_TIME is added to ensure that the next refresh happens after the apiQuota renewal.
     *
     * @param apiQuota The remaining allowed api request until the next api quota renewal.
     *                 Pre-condition: >= 0
     * @param remainingTimeInMins The remaining time left until the next api quota renewal.
     *                           Pre-condition: >= 0
     * @param apiCallsUsedInPreviousRefresh The amount of api used in the last api refresh.
     *                                      Pre-condition: >= 0
     * @param apiQuotaBuffer The amount of api calls that is set aside for manual operations by the user.
     *                       Pre-condition: >= 0
     * @param minRefreshPeriod The minimal refresh period that will be used.
     *                         There are 3 conditions that this will be used.
     *                         1) During application initialisation, when apiCallsUsedInPreviousRefresh is zero.
     *                         2) When computed refresh rate is < minRefreshPeriod.
     *                         3) When remainingTimeInMins = 0. This indicates that refreshes can occur in the
     *                            nearest time possible.
     *                            Recommended value : RefreshTimer.DEFAULT_REFRESH_PERIOD_IN_MINS
     *                         Pre-condition: > 0
     * @return Returns computed refresh period.
     */
    @SuppressWarnings("PMD")
    public static long computeRefreshTimerPeriod(int apiQuota, long remainingTimeInMins,
                                                 int apiCallsUsedInPreviousRefresh,
                                                 int apiQuotaBuffer, int minRefreshPeriod) {

        assert apiQuota >= 0 && remainingTimeInMins >= 0 && apiCallsUsedInPreviousRefresh >= 0
                && minRefreshPeriod > 0 && apiQuotaBuffer >= 0;

        if ((apiQuota > apiQuotaBuffer && apiCallsUsedInPreviousRefresh == 0) || remainingTimeInMins == 0) {
            return minRefreshPeriod;
        }

        long refreshTimeInMins;

        if (isQuotaInsufficient(apiQuota, apiCallsUsedInPreviousRefresh)) {
            refreshTimeInMins = remainingTimeInMins + BUFFER_TIME;
            return Math.max(refreshTimeInMins, minRefreshPeriod);
        }

        int remainingApiQuota = apiQuota - apiQuotaBuffer;
        int noOfRefreshAllowed = remainingApiQuota / apiCallsUsedInPreviousRefresh;
        refreshTimeInMins = (long) Math.ceil(remainingTimeInMins / (double) noOfRefreshAllowed);

        if (refreshTimeInMins == remainingTimeInMins) {
            refreshTimeInMins = refreshTimeInMins + BUFFER_TIME;
        }

        return Math.max(refreshTimeInMins, minRefreshPeriod);
    }

    /**
     * Changes the timer refresh period.
     * @param periodInMins The period of the TickingTimer in minutes.
     */
    public void changeRefreshPeriod(int periodInMins) {
        this.changePeriod((int) Utility.minsToSecs(periodInMins));
    }

    private static boolean isQuotaInsufficient(int apiQuota, int apiCallsUsedInPreviousRefresh) {
        boolean isBelowApiQuotaBufferAllowance = apiQuota <= API_QUOTA_BUFFER;
        boolean isOffsetQuotaLessThanApiCallsUsedInPreviousRefresh = apiQuota - API_QUOTA_BUFFER > 0
                                                        && apiQuota - API_QUOTA_BUFFER < apiCallsUsedInPreviousRefresh;

        return isBelowApiQuotaBufferAllowance || isOffsetQuotaLessThanApiCallsUsedInPreviousRefresh;
    }
}
