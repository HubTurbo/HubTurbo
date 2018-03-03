package util.events;

import backend.github.ApiQuotaInfo;

/**
 * An event that is generated when new API quota information is available.
 */
public class NewApiQuotaInfoAvailableEvent extends Event{

    ApiQuotaInfo apiQuotaInfo;

    public NewApiQuotaInfoAvailableEvent(ApiQuotaInfo apiQuotaInfo) {
        this.apiQuotaInfo = apiQuotaInfo;
    }

    public ApiQuotaInfo getApiQuotaInfo() {
        return apiQuotaInfo;
    }
}
