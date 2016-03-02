package util.events;

import ui.issuepanel.FilterPanel;

public class ApplyingFilterEvent extends Event {
    public final FilterPanel panel;

    public ApplyingFilterEvent(FilterPanel panel) {
        this.panel = panel;
    }
}
