package util.events;

import backend.github.ApiQuotaInfo;

/**
 * An event that is generated when the refresh timer is triggered.
 */
public class RefreshTimerTriggeredEvent extends Event{

    ApiQuotaInfo apiQuotaInfo;

    public RefreshTimerTriggeredEvent(ApiQuotaInfo apiQuotaInfo) {
        this.apiQuotaInfo = apiQuotaInfo;
    }

    public ApiQuotaInfo getApiQuotaInfo() {
        return apiQuotaInfo;
    }
}
