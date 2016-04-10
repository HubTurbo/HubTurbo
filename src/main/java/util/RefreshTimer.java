package util;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * This class is a subclass of TickingTimer. It contains logic to facilitate dynamic period of the timer.
 */
public class RefreshTimer extends TickingTimer{

    /**
     * The buffer time to be added to ensure refresh happens after the api quota renewal.
     */
    public static final int BUFFER_TIME = 1;

    /**
     * The amount that is set aside for manual operations by the user.
     */
    public static final int APIQUOTA_BUFFER = 200;

    /**
     * The default refresh period of the refresh rate.
     */
    public static final int DEFAULT_REFRESH_PERIOD_IN_MIN = 1;

    public RefreshTimer(String name, int period, Consumer<Integer> onTick, Runnable onTimeout,
                        TimeUnit timeUnit) {
        super(name, period, onTick, onTimeout, timeUnit);
    }

    /**
     * Computes the TickerTimer period that are used for refreshing the issues periodically.
     * The refresh rate is computed based on the three input argument and constant APIQUOTA_BUFFER.
     * APIQUOTA_BUFFER is the amount that is reserved for manual operations performed by the user.
     * This amount is then deducted from the actual apiQuota to ensure the algorithm reserves this amount.
     * The calculation has 2 steps.
     * The first step, the offset amount(apiQuota - APIQUOTA_BUFFER) is divided by the amount
     * of api used during the last refresh. This amount corresponds to the estimated no of refresh allowed to use till
     * the next apiQuota renewal.
     * Second, the refresh rate is calculated by dividing the remainingTimeInMin by the estimated no of refresh allowed.
     *
     * For some case where the refresh rate is equal to the remainingTimeInMin till next apiQuota renewal. BUFFER_TIME
     * is added to ensure that the next refresh happens after the apiQuota renewal.
     *
     * @param apiQuota The remaining allowed api request until the next api request allowance renewal.
     * @param remainingTimeInMin The remaining time left until the next api request allowance renewal.
     * @param lastApiCallsUsed The amount of api used in the last api pull.
     * @param bufferTimeInMin The amount of time in min to be added to the refreshTime in cases when computed
     *                        refresh period equals to remainingTimeInMin. Recommended value : RefreshTimer.BUFFER_TIME
     * @param defaultRefreshPeriodInMin : The default refresh period in min that will be used when input arguments
     *                                    value is not valid for calculation of refresh rate. In those cases,
     *                                    the default value is deemed to be the most proper refresh rate.
     *                                    Recommended value : RefreshTimer.DEFAULT_REFRESH_PERIOD_IN_MIN
     * @return Computed refresh period.
     */
    public static long computeRefreshTimerPeriod(int apiQuota, long remainingTimeInMin, int lastApiCallsUsed,
                                                 int bufferTimeInMin, int defaultRefreshPeriodInMin) {

        assert apiQuota >= 0 && remainingTimeInMin >= 0 && lastApiCallsUsed >= 0 && bufferTimeInMin >= 0
                && defaultRefreshPeriodInMin > 0;

        long refreshTimeInMin;

        boolean isDuringAppInit = apiQuota != 0 && lastApiCallsUsed == 0;
        if (isDuringAppInit || remainingTimeInMin == 0) {
            refreshTimeInMin = defaultRefreshPeriodInMin;
            return refreshTimeInMin;
        }

        if (isQuotaInsufficient(apiQuota, lastApiCallsUsed)) {
            refreshTimeInMin = remainingTimeInMin + bufferTimeInMin;
            return refreshTimeInMin;
        }

        double noOfRefreshAllowed = computeNoOfRefreshAllowed(apiQuota, lastApiCallsUsed);
        refreshTimeInMin = (long) Math.ceil(remainingTimeInMin / noOfRefreshAllowed);

        if (refreshTimeInMin == remainingTimeInMin) {
            refreshTimeInMin = refreshTimeInMin + bufferTimeInMin;
        }
        return refreshTimeInMin;

    }

    private static double computeNoOfRefreshAllowed(int apiQuota, int lastApiCallsUsed) {
        return Math.floor((apiQuota - APIQUOTA_BUFFER) / (double) lastApiCallsUsed);
    }

    /**
     * Changes the timer refresh period.
     * @param periodInMin The period of the TickingTimer in minute.
     */
    public void changeRefreshPeriod(int periodInMin) {
        this.changePeriod((int) Utility.minsToSecs(periodInMin));
    }

    private static boolean isQuotaInsufficient(int apiQuota, int lastApiCallsUsed) {
        boolean isOutOfQuota = apiQuota == 0 && lastApiCallsUsed == 0;
        boolean isBelowApiQuotaBufferAllowance = apiQuota <= APIQUOTA_BUFFER;
        boolean isOffsetQuotaLessThanLastApiCallsUsed = apiQuota - APIQUOTA_BUFFER > 0
                                                        && apiQuota - APIQUOTA_BUFFER < lastApiCallsUsed;

        return isOutOfQuota || isBelowApiQuotaBufferAllowance || isOffsetQuotaLessThanLastApiCallsUsed;
    }
}
