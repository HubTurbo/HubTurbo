package util.events;

public class PanelClickedEvent extends Event {
    public int panelIndex;

    public PanelClickedEvent(int panelIndex) {
        this.panelIndex = panelIndex;
    }
}
