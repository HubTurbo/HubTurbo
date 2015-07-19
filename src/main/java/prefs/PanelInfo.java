package prefs;

public class PanelInfo {
    
    private String name;
    private String filter;
    
    public PanelInfo(String name, String filter) {
        this.name = name;
        this.filter = filter;
    }
    
    public void setPanelName(String name) {
        this.name = name;
    }
    
    public String getPanelName() {
        return this.name;
    }
    
    public void setPanelFilter(String filter) {
        this.filter = filter;
    }
    
    public String getPanelFilter() {
        return this.filter;
    }

}
