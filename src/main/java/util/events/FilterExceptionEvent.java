package util.events;

import ui.issuepanel.FilterPanel;

/**
 * This class is meant to indicate that there is an exception thrown during the filtering of issues
 */
public class FilterExceptionEvent extends Event {
    public FilterPanel panel;
    public String exceptionMessage;

    public FilterExceptionEvent(FilterPanel panel, String exceptionMessage) {
        this.panel = panel;
        this.exceptionMessage = exceptionMessage;
    }
}
