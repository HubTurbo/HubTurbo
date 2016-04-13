package util.events;

/**
 * An event that is called when the refresh timer is triggered.
 */
public class RefreshTimerTriggeredEvent extends RateLimitsUpdatedEvent {

    public RefreshTimerTriggeredEvent(int remainingRequests, long nextRefreshInMillisecs) {
        super(remainingRequests, nextRefreshInMillisecs);
    }

}
