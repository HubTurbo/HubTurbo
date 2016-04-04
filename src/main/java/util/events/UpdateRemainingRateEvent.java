package util.events;

/**
 * An event to call tasks related to the api remaining rate.
 */
public class UpdateRemainingRateEvent extends UpdateRateLimitsEvent{

    public UpdateRemainingRateEvent(int remainingRequests, long nextRefreshInMillisecs) {
        super(remainingRequests, nextRefreshInMillisecs);
    }

}
