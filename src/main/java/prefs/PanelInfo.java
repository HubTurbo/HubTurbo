package prefs;

public class PanelInfo {

    private final String name;
    private final String filter;

    public PanelInfo(String name, String filter) {
        this.name = name;
        this.filter = filter;
    }

    public PanelInfo() {
        this.name = "Panel";
        this.filter = "";
    }

    public String getPanelName() {
        return this.name;
    }

    public String getPanelFilter() {
        return this.filter;
    }

}
