package util.events;

import ui.issuepanel.FilterPanel;

public class PanelLoadingEvent extends Event {
    public final FilterPanel panel;

    public PanelLoadingEvent(FilterPanel panel) {
        this.panel = panel;
    }
}
