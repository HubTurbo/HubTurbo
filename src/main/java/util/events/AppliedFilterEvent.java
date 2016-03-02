package util.events;

import ui.issuepanel.FilterPanel;

public class AppliedFilterEvent extends Event {
    public final FilterPanel panel;

    public AppliedFilterEvent(FilterPanel panel) {
        this.panel = panel;
    }
}
