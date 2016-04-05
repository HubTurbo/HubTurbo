package prefs;

import java.util.Objects;

/**
 * Represents the information about a Panel.
 */
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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        PanelInfo panelInfo = (PanelInfo) object;
        return Objects.equals(name, panelInfo.name) && Objects.equals(filter, panelInfo.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, filter);
    }
}
