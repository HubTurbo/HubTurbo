package util;

/**
 * Provides methods to better manage the API quota usage.
 */
public class ApiQuotaManager {

    /**
     * The amount that is set aside for manual operations by the user.
     */
    public static final int API_QUOTA_BUFFER = 200;

    /**
     * The default refresh period.
     */
    public static final int DEFAULT_REFRESH_PERIOD_IN_MINS = 1;

    /**
     * Computes the TickerTimer period that is used for refreshing the issues periodically
     * Assumes future refreshes will take the same number of API calls as the last refresh and find out the refresh
     * duration that will spread out the refreshes until the next API quota top up.
     *
     * For some cases where the computed refresh rate is equal to the remainingTimeInMins,
     * BUFFER_TIME is added to ensure that the next refresh happens after the apiQuota renewal.
     *
     * PMD is suppressed to allow explicit parenthesis.
     *
     * @param apiQuota The remaining allowed API requests until the next API quota renewal.
     *                 Pre-condition: >= 0
     * @param remainingTimeInMins The remaining time left until the next API quota renewal.
     *                           Pre-condition: >= 0
     * @param apiCallsUsedInPreviousRefresh The amount of API used in the last API refresh.
     *                                      Pre-condition: >= 0
     * @param apiQuotaBuffer The amount of API calls that is set aside for manual operations by the user.
     *                       Pre-condition: >= 0
     * @param minRefreshPeriod The minimal refresh period that will be used.
     *                         There are 3 conditions that this will be used.
     *                         1) During application initialisation, when apiCallsUsedInPreviousRefresh is zero.
     *                         2) When computed refresh rate is < minRefreshPeriod.
     *                         3) When remainingTimeInMins = 0. This indicates that refreshes can occur in the
     *                            nearest time possible.
     *                            Recommended value : ApiQuotaManager.DEFAULT_REFRESH_PERIOD_IN_MINS
     *                         Pre-condition: > 0
     * @return the computed refresh period.
     */
    @SuppressWarnings("PMD")
    public static long computeRefreshTimerPeriod(int apiQuota, long remainingTimeInMins,
                                                 int apiCallsUsedInPreviousRefresh,
                                                 int apiQuotaBuffer, int minRefreshPeriod) {

        assert apiQuota >= 0 && remainingTimeInMins >= 0 && apiCallsUsedInPreviousRefresh >= 0
                && minRefreshPeriod > 0 && apiQuotaBuffer >= 0;

        final int bufferTime = 1;

        if ((apiQuota > apiQuotaBuffer && apiCallsUsedInPreviousRefresh == 0) || remainingTimeInMins == 0) {
            return minRefreshPeriod;
        }

        long refreshTimeInMins;

        if (isQuotaInsufficient(apiQuota, apiCallsUsedInPreviousRefresh, apiQuotaBuffer)) {
            refreshTimeInMins = remainingTimeInMins + bufferTime;
            return Math.max(refreshTimeInMins, minRefreshPeriod);
        }

        int remainingApiQuota = apiQuota - apiQuotaBuffer;
        int noOfRefreshAllowed = remainingApiQuota / apiCallsUsedInPreviousRefresh;
        refreshTimeInMins = (long) Math.ceil(remainingTimeInMins / (double) noOfRefreshAllowed);

        if (refreshTimeInMins == remainingTimeInMins) {
            refreshTimeInMins = refreshTimeInMins + bufferTime;
        }

        return Math.max(refreshTimeInMins, minRefreshPeriod);
    }

    private static boolean isQuotaInsufficient(int apiQuota, int apiCallsUsedInPreviousRefresh, int apiQuotaBuffer) {
        boolean isBelowApiQuotaBufferAllowance = apiQuota <= apiQuotaBuffer;
        boolean isOffsetQuotaLessThanApiCallsUsedInPreviousRefresh = apiQuota - apiQuotaBuffer > 0
                && apiQuota - apiQuotaBuffer < apiCallsUsedInPreviousRefresh;

        return isBelowApiQuotaBufferAllowance || isOffsetQuotaLessThanApiCallsUsedInPreviousRefresh;
    }
}
