package ui;

import com.google.gson.Gson;

public class DragData {

    public enum Source {
        ISSUE_CARD, PANEL, LABEL_TAB, MILESTONE_TAB, ASSIGNEE_TAB, FEED_TAB
    }

    private Source source;
    private int panelIndex;
    private int issueIndex;

    private String entityName;

    public DragData(Source source, int panelIndex, int issue) {
        this.setSource(source);
        this.panelIndex = panelIndex;
        this.issueIndex = issue;
    }

//    public DragData(Source source, String name) {
//        this.setSource(source);
//        this.entityName = name;
//    }

    public int getPanelIndex() {
        return panelIndex;
    }

//    public void setPanelIndex(int panelIndex) {
//        this.panelIndex = panelIndex;
//    }

//    public int getIssueIndex() {
//        return issueIndex;
//    }

//    public void setIssueIndex(int issueIndex) {
//        this.issueIndex = issueIndex;
//    }

    public String serialise() {
        return (new Gson()).toJson(this);
    }

    public static DragData deserialise(String json) {
        return (new Gson()).fromJson(json, DragData.class);
    }

//    public Source getSource() {
//        return source;
//    }

    public void setSource(Source source) {
        this.source = source;
    }

//    public String getEntityName() {
//        return entityName;
//    }
}
