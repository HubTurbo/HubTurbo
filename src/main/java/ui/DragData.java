package ui;

import com.google.gson.Gson;

public class DragData {

    public enum Source {
        ISSUE_CARD, PANEL, LABEL_TAB, MILESTONE_TAB, ASSIGNEE_TAB, FEED_TAB
    }

    private final int panelIndex;

    public DragData(int panelIndex) {
        this.panelIndex = panelIndex;
    }

    public int getPanelIndex() {
        return panelIndex;
    }

    public String serialise() {
        return new Gson().toJson(this);
    }

    public static DragData deserialise(String json) {
        return new Gson().fromJson(json, DragData.class);
    }
}
