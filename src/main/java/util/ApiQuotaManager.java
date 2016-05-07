package util;

import backend.Logic;
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
     * The amount of the available remaining API quota.
     */
    private int remainingApiQuota = UNINITIALIZED;

    /**
     * The number of API calls used in most recent refresh.
     */
    private int apiCallsUsedInRecentRefresh = UNINITIALIZED;

    /**
     * The duration between each refresh of the data store.
     */
    private long refreshDurationInMinutes = DEFAULT_REFRESH_PERIOD_IN_MINS;

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
        updateSyncRefreshRate(e.getApiQuotaInfo().getRemainingQuota(),
                              e.getApiQuotaInfo().minutesToNextQuotaTopup());
    }

    private void updateSyncRefreshRate(int newRemainingApiQuota, long minutesToNextQuotaTopup) {

        if (remainingApiQuota == UNINITIALIZED){
            remainingApiQuota = newRemainingApiQuota;
            return;
        }

        // apiCallsUsedInRecentRefresh will not get initialised in the first attempt if an API quota top up has occurred
        // since the recent refresh.
        if (apiCallsUsedInRecentRefresh == UNINITIALIZED){
            if (remainingApiQuota >= newRemainingApiQuota){
                apiCallsUsedInRecentRefresh = remainingApiQuota - newRemainingApiQuota;
            } else {
                remainingApiQuota = newRemainingApiQuota;
                return;
            }
        }

        // remainingApiQuota < newRemainingApiQuota indicates that an API quota top up has occurred
        // since the recent refresh.
        apiCallsUsedInRecentRefresh = remainingApiQuota < newRemainingApiQuota
                                      ? apiCallsUsedInRecentRefresh : remainingApiQuota - newRemainingApiQuota;

        remainingApiQuota = newRemainingApiQuota;

        refreshDurationInMinutes = computeRefreshTimerPeriod(newRemainingApiQuota,
                                                             minutesToNextQuotaTopup,
                                                             apiCallsUsedInRecentRefresh,
                                                             API_QUOTA_BUFFER, DEFAULT_REFRESH_PERIOD_IN_MINS);
        this.refreshTimer.restartTimer((int) Utility.minsToSecs(refreshDurationInMinutes));

        logger.info("Refresh period updated to " + refreshDurationInMinutes
                + "mins with API calls used in recent refresh cycle is " + apiCallsUsedInRecentRefresh
                + ", current API quota is " + newRemainingApiQuota + " and next API quota top-up in "
                + minutesToNextQuotaTopup + "mins.");
    }

    /**
     * Computes the refreshTimer period that is used for refreshing the issues periodically.
     * Assumes future refreshes will take the same number of API calls as the last refresh and finds out the refresh
     * duration that will spread out the refreshes until the next API quota top up.
     *
     * @param apiQuota The remaining API quota until the next API quota renewal.
     *                 Pre-condition: >= 0
     * @param minutesToNextQuotaTopup The remaining time left until the next API quota renewal.
     *                            Pre-condition: >= 0
     * @param apiCallsUsedInRecentRefresh The amount of API used in the recent API refresh.
     *                                      Pre-condition: >= 0
     * @param apiQuotaBuffer The amount of API calls that is set aside for manual operations by the user.
     *                       Pre-condition: >= 0
     * @param minRefreshPeriod The minimal refresh period that will be used.
     *                         Recommended value : ApiQuotaManager.DEFAULT_REFRESH_PERIOD_IN_MINS
     *                         Pre-condition: > 0
     * @return the computed refresh period or minRefreshPeriod.
     *          minRefreshPeriod will be returned if one of the following conditions are met:
     *          1) When apiCallsUsedInRecentRefresh == 0 (i.e. when no API quota is used in recent refresh)
     *          2) When computed refresh period < minRefreshPeriod
     *                  (i.e. when the remaining API quota is enough to maintain the min refresh rate)
     *          3) When minutesToNextQuotaTopup == 0 (i.e. when a refresh is due to happen inside the next minute).
     */
    @SuppressWarnings("PMD") // PMD warning is suppressed to allow explicit parenthesis.
    public static long computeRefreshTimerPeriod(int apiQuota, long minutesToNextQuotaTopup,
                                                 int apiCallsUsedInRecentRefresh,
                                                 int apiQuotaBuffer, int minRefreshPeriod) {

        assert apiQuota >= 0 && minutesToNextQuotaTopup >= 0 && apiCallsUsedInRecentRefresh >= 0
                && apiQuotaBuffer >= 0 && minRefreshPeriod > 0;

        int usableApiQuota = apiQuota - apiQuotaBuffer;

        if (minutesToNextQuotaTopup == 0 || apiCallsUsedInRecentRefresh == 0) {
            return minRefreshPeriod;
        }

        long refreshTimeInMins;

        boolean isQuotaInsufficient = usableApiQuota <= 0 || usableApiQuota < apiCallsUsedInRecentRefresh;

        if (isQuotaInsufficient) {
            refreshTimeInMins = minutesToNextQuotaTopup;
            return Math.max(refreshTimeInMins, minRefreshPeriod);
        }

        int noOfRefreshAllowed = usableApiQuota / apiCallsUsedInRecentRefresh;
        refreshTimeInMins = (long) Math.ceil(minutesToNextQuotaTopup / (double) noOfRefreshAllowed);

        return Math.max(refreshTimeInMins, minRefreshPeriod);
    }
}
