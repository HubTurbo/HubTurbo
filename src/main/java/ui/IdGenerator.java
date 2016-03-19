package ui;

public final class IdGenerator {
    private IdGenerator() {}

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

    public static String getPanelId(String repoId, int panelIndex) {
        return repoId + "_col" + panelIndex;
    }

    public static String getLabelPickerTextFieldId() {
        return "queryField";
    }

    public static String getPanelCloseButton(String repoId, int panelIndex) {
        return repoId + "_col" + panelIndex + "_closeButton";
    }


    public static String getPanelCellIdForTest(String repoId, int panelIndex, int issueId) {
        return "#" + getPanelCellId(repoId, panelIndex, issueId);
    }

    public static String getPanelFilterTextFieldIdForTest(String repoId, int panelIndex) {
        return "#" + getPanelFilterTextFieldId(repoId, panelIndex);
    }

    public static String getRepositorySelectorIdForTest() {
        return "#" + getRepositorySelectorId();
    }

    public static String getPanelNameAreaIdForTest(String repoId, int panelIndex) {
        return "#" + getPanelNameAreaId(repoId, panelIndex);
    }

    public static String getPanelIdForTest(String repoId, int panelIndex) {
        return "#" + getPanelId(repoId, panelIndex);
    }

    public static String getLabelPickerTextFieldIdForTest() {
        return "#" + getLabelPickerTextFieldId();
    }

    public static String getPanelCloseButtonForTest(String repoId, int panelIndex) {
        return "#" + getPanelCloseButton(repoId, panelIndex);
    }
}
