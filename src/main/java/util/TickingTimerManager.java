package util;

/**
 * This class manages the TickingTimer instance used for refreshing. It computes the refresh rate period and change
 * the refresh period.
 */
public class TickingTimerManager {

    /**
     * To allow some additional time until next refresh.
     */
    public static final int BUFFER_TIME = 1;

    /**
     * The amount that is set aside for manual refresh or creation of issues, labels and milestones.
     */
    public static final int APIQUOTA_BUFFER = 200;

    public static final int NO_OF_REFRESH_THAT_REQUIRES_MORE_TIME = 1;
    public static final int DEFAULT_REFRESH_PERIOD_IN_SEC = 60;

    private final TickingTimer timer;

    public TickingTimerManager(TickingTimer timer) {
        this.timer = timer;
    }

    public void startTimer() {
        timer.start();
    }

    public void restartTimer() {
        timer.restart();
    }

    /**
     * Computes the TickerTimer period that are used for refreshing the issues periodically.
     * The refresh rate is computed based on the three input argument and constant APIQUOTA_BUFFER.
     * APIQUOTA_BUFFER is the amount that is reserved for manual refresh and creation of label, issue and milestone.
     * This amount is then deducted from the actual apiQuota to ensure the algorithm reserve this amount.
     * The calculation has 2 steps.
     * The first step, the offset amount(apiQuota - APIQUOTA_BUFFER) is divided by the amount
     * of api used during the last refresh. This amount corresponds to the estimated no of refresh allowed to use till
     * the next apiQuota renewal.
     * Second, the refresh rate is calculated by dividing the remainingTime by the estimated no of refresh allowed.
     *
     * For some case where the refresh rate is equal to the remainingTime till next apiQuota renewal. BUFFER_TIME
     * is added to ensure that the next refresh happens after the apiQuota renewal.
     *
     * @param apiQuota         : The remaining allowed api request until the next api request allowance renewal.
     * @param remainingTime    : The remaining time left until the next api request allowance renewal.
     * @param lastApiCallsUsed : The amount of api used in the last api pull.
     */
    public long computeTickerTimerPeriod(int apiQuota, long remainingTime, int lastApiCallsUsed) {

        assert apiQuota >= 0 && remainingTime >= 0 && lastApiCallsUsed >= 0;

        long refreshTimeInMin;

        boolean isDuringAppInit = apiQuota != 0 && lastApiCallsUsed == 0;
        if (isDuringAppInit || remainingTime == 0) {
            refreshTimeInMin = Utility.secsToMins(DEFAULT_REFRESH_PERIOD_IN_SEC);
            return refreshTimeInMin;
        }

        if (isQuotaInsufficient(apiQuota, lastApiCallsUsed)) {
            refreshTimeInMin = remainingTime + BUFFER_TIME;
            return refreshTimeInMin;
        } else {
            double noOfRefreshAllowed = computeNoOfRefreshAllowed(apiQuota, lastApiCallsUsed);
            refreshTimeInMin = (long) Math.ceil(remainingTime / noOfRefreshAllowed);

            if (noOfRefreshAllowed == NO_OF_REFRESH_THAT_REQUIRES_MORE_TIME) {
                refreshTimeInMin = refreshTimeInMin + BUFFER_TIME;
            }
            return refreshTimeInMin;
        }
    }

    private double computeNoOfRefreshAllowed(int apiQuota, int lastApiCallsUsed) {
        return Math.floor((apiQuota - APIQUOTA_BUFFER) / (double) lastApiCallsUsed);
    }

    /**
     * Changes the timer refresh period.
     * @param period
     */
    public void changeTickingTimerPeriodInMins(int period) {
        timer.changePeriodInSecs((int) Utility.minsToSecs(period));
    }

    private boolean isQuotaInsufficient(int apiQuota, int lastApiCallsUsed) {
        boolean isOutOfQuota = apiQuota == 0 && lastApiCallsUsed == 0;
        boolean isBelowApiQuotaBufferAllowance = apiQuota <= APIQUOTA_BUFFER;
        boolean isOffsetQuotaLessThanLastApiCallsUsed = apiQuota - APIQUOTA_BUFFER > 0
                                                      && apiQuota - APIQUOTA_BUFFER < lastApiCallsUsed;

        return isOutOfQuota || isBelowApiQuotaBufferAllowance || isOffsetQuotaLessThanLastApiCallsUsed;
    }
}
