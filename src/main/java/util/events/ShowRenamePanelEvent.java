package util.events;

public class ShowRenamePanelEvent extends Event {

    public int panelId;

    public ShowRenamePanelEvent(int panelId) {
        this.panelId = panelId;
    }

}
