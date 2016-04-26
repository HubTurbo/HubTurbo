package util;

import backend.Logic;
import backend.github.ApiQuotaInfo;
import org.apache.logging.log4j.Logger;
import ui.UI;
import util.events.RefreshTimerTriggeredEvent;
import util.events.RefreshTimerTriggeredEventHandler;

import java.util.concurrent.TimeUnit;

/**
 * Manages the API quota usage and periodic refreshes of the data store.
 */
public class ApiQuotaManager {

    private static final Logger logger = HTLog.get(ApiQuotaManager.class);

    /**
     * The amount that is set aside for manual operations by the user.
     */
    private static final int API_QUOTA_BUFFER = 200;

    /**
     * The default refresh period.
     */
    private static final int DEFAULT_REFRESH_PERIOD_IN_MINS = 1;

    /**
     * The uninitialized value for uninitialized variables.
     */
    private static final int UNINITIALIZED = -1;

    /**
     * The amount of the available remaining API requests.
     */
    private int remainingApiRequests = UNINITIALIZED;

    /**
     * The number of API calls used in most recent refresh.
     */
    private int apiCallsUsedInPreviousRefresh = 0;

    /**
     * The duration between each refresh of the data store.
     */
    private long refreshDurationInMinutes = 1;

    private final TickingTimer refreshTimer;

    public ApiQuotaManager(Logic logic) {
        this.refreshTimer = new TickingTimer("Refresh Timer",
                (int) Utility.minsToSecs(DEFAULT_REFRESH_PERIOD_IN_MINS),
                UI.status::updateTimeToRefresh, logic::refresh, TimeUnit.SECONDS);
        this.refreshTimer.start();
        UI.events.registerEvent((RefreshTimerTriggeredEventHandler) this::updateSyncRefreshRate);
    }

    /**
     * @return The duration between each refresh of the data store.
     */
    public long getRefreshDurationInMinutes() {
        return refreshDurationInMinutes;
    }

    /**
     * Restarts the refreshTimer.
     */
    public void restartRefreshTimer(){
        this.refreshTimer.restart();
    }

    /**
     * Updates the period of the refreshTimer for synchronization of the data store.
     * The initial call to this method does not update the period of the refreshTimer.
     * @param e RefreshTimerTriggeredEvent object that holds the current API quota information.
     */
    private void updateSyncRefreshRate(RefreshTimerTriggeredEvent e) {
        ApiQuotaInfo info = e.getApiQuotaInfo();

        if (remainingApiRequests == UNINITIALIZED){
            remainingApiRequests = info.getRemainingRequests();
            return;
        }

        apiCallsUsedInPreviousRefresh = computeApiCallsUsedInPreviousRefresh(info.getRemainingRequests());
        remainingApiRequests = info.getRemainingRequests();
        refreshDurationInMinutes = computeRefreshTimerPeriod(info.getRemainingRequests(),
                                                             Utility.minutesFromNow(info.getNextRefreshInMillisecs()),
                                                             apiCallsUsedInPreviousRefresh, API_QUOTA_BUFFER,
                                                             DEFAULT_REFRESH_PERIOD_IN_MINS);
        this.refreshTimer.restartTimer((int) Utility.minsToSecs(refreshDurationInMinutes));
        logger.info("Refresh period updated to " + refreshDurationInMinutes
                + "mins with API calls used in previous refresh cycle is " + apiCallsUsedInPreviousRefresh
                + ", current API quota is " + info.getRemainingRequests() + " and next API quota top-up in "
                + info.getNextRefreshInMinutesFromNow() + "mins.");
    }

    /**
     * Computes the API calls used in previous refresh.
     * @param newRemainingApiRequests The number of API requests remaining in the current API quota window.
     * @return The number of API calls used in previous refresh.
     *         apiCallsUsedInPreviousRefresh will be returned when newRemainingApiRequests > remainingApiRequests.
     */
    private int computeApiCallsUsedInPreviousRefresh(int newRemainingApiRequests) {
        int apiUsageInRecentRefresh = remainingApiRequests - newRemainingApiRequests;

        if (apiUsageInRecentRefresh < 0) {
            return apiCallsUsedInPreviousRefresh;
        }

        return apiUsageInRecentRefresh;
    }

    /**
     * Computes the TickerTimer period that is used for refreshing the issues periodically
     * Assumes future refreshes will take the same number of API calls as the last refresh and finds out the refresh
     * duration that will spread out the refreshes until the next API quota top up.
     *
     * Calculation avoids scheduling a refresh that happens during the same time as the API quota top up.
     *
     * PMD warning is suppressed to allow explicit parenthesis.
     *
     * @param apiQuota The remaining allowed API requests until the next API quota renewal.
     *                 Pre-condition: >= 0
     * @param remainingTimeInMins The remaining time left until the next API quota renewal.
     *                           Pre-condition: >= 0
     * @param apiCallsUsedInPreviousRefresh The amount of API used in the previous API refresh.
     *                                      Pre-condition: >= 0
     * @param apiQuotaBuffer The amount of API calls that is set aside for manual operations by the user.
     *                       Pre-condition: >= 0
     * @param minRefreshPeriod The minimal refresh period that will be used.
     *                         Recommended value : ApiQuotaManager.DEFAULT_REFRESH_PERIOD_IN_MINS
     *                         Pre-condition: > 0
     * @return the computed refresh period or minRefreshPeriod.
     *          There are 3 conditions that minRefreshPeriod will be used.
     *          1) During application initialisation, when apiCallsUsedInPreviousRefresh is zero.
     *          2) When computed refresh rate is < minRefreshPeriod.
     *          3) When remainingTimeInMins = 0. This indicates that refreshes can occur in the
     *             nearest time possible.
     */
    @SuppressWarnings("PMD")
    public static long computeRefreshTimerPeriod(int apiQuota, long remainingTimeInMins,
                                                 int apiCallsUsedInPreviousRefresh,
                                                 int apiQuotaBuffer, int minRefreshPeriod) {

        assert apiQuota >= 0 && remainingTimeInMins >= 0 && apiCallsUsedInPreviousRefresh >= 0
                && apiQuotaBuffer >= 0 && minRefreshPeriod > 0;

        int remainingApiQuota = apiQuota - apiQuotaBuffer;

        if ((remainingApiQuota > 0 && apiCallsUsedInPreviousRefresh == 0) || remainingTimeInMins == 0) {
            return minRefreshPeriod;
        }

        long refreshTimeInMins;
        final int bufferTime = 1;

        if (isQuotaInsufficient(remainingApiQuota, apiCallsUsedInPreviousRefresh)) {
            refreshTimeInMins = remainingTimeInMins + bufferTime;
            return Math.max(refreshTimeInMins, minRefreshPeriod);
        }

        int noOfRefreshAllowed = remainingApiQuota / apiCallsUsedInPreviousRefresh;
        refreshTimeInMins = (long) Math.ceil(remainingTimeInMins / (double) noOfRefreshAllowed);

        if (refreshTimeInMins == remainingTimeInMins) {
            refreshTimeInMins = refreshTimeInMins + bufferTime;
        }

        return Math.max(refreshTimeInMins, minRefreshPeriod);
    }

    private static boolean isQuotaInsufficient(int remainingApiQuota, int apiCallsUsedInPreviousRefresh) {
        boolean isBelowApiQuotaBufferAllowance = remainingApiQuota <= 0;
        boolean isOffsetQuotaLessThanApiCallsUsedInPreviousRefresh = remainingApiQuota > 0
                && remainingApiQuota < apiCallsUsedInPreviousRefresh;

        return isBelowApiQuotaBufferAllowance || isOffsetQuotaLessThanApiCallsUsedInPreviousRefresh;
    }
}
