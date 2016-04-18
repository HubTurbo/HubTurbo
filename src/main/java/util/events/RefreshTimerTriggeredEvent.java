package util.events;

/**
 * An event that is generated when the refresh timer is triggered.
 */
public class RefreshTimerTriggeredEvent extends NewApiQuotaInfoAvailableEvent {

    public RefreshTimerTriggeredEvent(int remainingRequests, long nextRefreshInMillisecs) {
        super(remainingRequests, nextRefreshInMillisecs);
    }

}
