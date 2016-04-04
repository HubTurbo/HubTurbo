package util.events;

/**
 * An event to update the sync refresh rate for periodical update of the data store.
 */
public class UpdateSyncRefreshRateEvent extends UpdateRateLimitsEvent{

    public UpdateSyncRefreshRateEvent(int remainingRequests, long nextRefreshInMillisecs) {
        super(remainingRequests, nextRefreshInMillisecs);
    }

}
