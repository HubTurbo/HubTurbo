package prefs;

public class PanelInfo {
    
    private String name;
    private String filter;
    
    public PanelInfo(String name, String filter) {
        this.name = name;
        this.filter = filter;
    }
    
    public String getPanelName() {
        return this.name;
    }
    
    public String getPanelFilter() {
        return this.filter;
    }

}
