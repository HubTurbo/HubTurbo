package ui;

public final class IdGenerator {
    private IdGenerator() {}

    public static String getPanelCellId(String repoId, int panelIndex, int issueId) {
        return repoId + "_col" + panelIndex + "_" + issueId;
    }

    public static String getPanelFilterTextFieldId(String repoId, int panelIndex) {
        return repoId + "_col" + panelIndex + "_filterTextField";
    }

    public static String getPanelRenameTextFieldId(String repoId, int panelIndex) {
        return repoId + "_col" + panelIndex + "_renameTextField";
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

    public static String getPanelCloseButtonId(String repoId, int panelIndex) {
        return repoId + "_col" + panelIndex + "_closeButton";
    }

    public static String getPanelRenameButtonId(String repoId, int panelIndex) {
        return repoId + "_col" + panelIndex + "_renameButton";
    }

    public static String getOcticonButtonId(String repoId, int panelIndex, String octiconCssName) {
        return repoId + "_col" + panelIndex + "_" + octiconCssName;
    }

    public static String getLoginDialogOwnerFieldId() {
        return "repoOwnerField";
    }



    public static String getPanelCellIdForTest(String repoId, int panelIndex, int issueId) {
        return "#" + getPanelCellId(repoId, panelIndex, issueId);
    }

    public static String getPanelFilterTextFieldIdForTest(String repoId, int panelIndex) {
        return "#" + getPanelFilterTextFieldId(repoId, panelIndex);
    }

    public static String getPanelRenameTextFieldIdForTest(String repoId, int panelIndex) {
        return "#" + getPanelRenameTextFieldId(repoId, panelIndex);
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

    public static String getPanelCloseButtonIdForTest(String repoId, int panelIndex) {
        return "#" + getPanelCloseButtonId(repoId, panelIndex);
    }

    public static String getPanelRenameButtonIdForTest(String repoId, int panelIndex) {
        return "#" + getPanelRenameButtonId(repoId, panelIndex);
    }

    public static String getOcticonButtonIdForTest(String repoId, int panelIndex, String octiconCssName) {
        return "#" + getOcticonButtonId(repoId, panelIndex, octiconCssName);
    }

    public static String getLoginDialogOwnerFieldIdForTest() {
        return "#" + getLoginDialogOwnerFieldId();
    }
}
