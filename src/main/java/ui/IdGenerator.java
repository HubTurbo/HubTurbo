package ui;

public class IdGenerator {
    public static String getPanelCellId(String repoId, int panelIndex, int issueId) {
        return repoId + "_col" + panelIndex + "_" + issueId;
    }

    public static String getPanelFilterTextFieldId(String repoId, int panelIndex) {
        return repoId + "_col" + panelIndex + "_filterTextField";
    }

    public static String getRepositorySelectorId() {
        return "repositorySelector";
    }

    public static String getPanelNameAreaId(String repoId, int panelIndex) {
        return repoId + "_col" + panelIndex + "_nameText";
    }
}
