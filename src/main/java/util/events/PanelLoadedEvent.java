package util.events;

import ui.issuepanel.FilterPanel;

public class PanelLoadedEvent extends Event {
    public final FilterPanel panel;

    public PanelLoadedEvent(FilterPanel panel) {
        this.panel = panel;
    }
}
