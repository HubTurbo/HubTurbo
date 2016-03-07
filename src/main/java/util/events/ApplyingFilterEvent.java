package util.events;

import ui.issuepanel.FilterPanel;

/**
 * The ApplyingFilterEvent is meant to indicate that the panel is being updated to match its new filter
 */
public class ApplyingFilterEvent extends Event {
    public final FilterPanel panel;

    public ApplyingFilterEvent(FilterPanel panel) {
        this.panel = panel;
    }
}
