package util.events;

import ui.issuepanel.FilterPanel;

/**
 * The AppliedFilterEvent is meant to indicate that the filtering of panel has completed
 */
public class AppliedFilterEvent extends Event {
    public final FilterPanel panel;

    public AppliedFilterEvent(FilterPanel panel) {
        this.panel = panel;
    }
}
