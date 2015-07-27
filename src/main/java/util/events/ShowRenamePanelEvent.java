package util.events;

import ui.issuepanel.FilterPanel;

public class ShowRenamePanelEvent extends Event {

    public FilterPanel panel;

    public ShowRenamePanelEvent(FilterPanel panel) {
        this.panel = panel;
    }

}
