package util.events;

public class ShowRenameTextFieldEvent extends Event {
    
    public int panelId;
    
    public ShowRenameTextFieldEvent(int panelId) {
        this.panelId = panelId;
    }

}
