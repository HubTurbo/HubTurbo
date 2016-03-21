package ui;

public final class IdGenerator {
    private IdGenerator() {}

    public static String getPanelCellId(int panelIndex, int issueId) {
        return "panel" + panelIndex + "_" + issueId;
    }

    public static String getPanelFilterTextFieldId(int panelIndex) {
        return "panel" + panelIndex + "_filterTextField";
    }

    public static String getPanelRenameTextFieldId(int panelIndex) {
        return "panel" + panelIndex + "_renameTextField";
    }

    public static String getRepositorySelectorId() {
        return "repositorySelector";
    }

    public static String getPanelNameAreaId(int panelIndex) {
        return "panel" + panelIndex + "_nameText";
    }

    public static String getPanelId(int panelIndex) {
        return "panel" + panelIndex;
    }

    public static String getLabelPickerTextFieldId() {
        return "queryField";
    }

    public static String getPanelCloseButtonId(int panelIndex) {
        return "panel" + panelIndex + "_closeButton";
    }

    public static String getPanelRenameButtonId(int panelIndex) {
        return "panel" + panelIndex + "_renameButton";
    }

    public static String getOcticonButtonId(int panelIndex, String octiconCssName) {
        return "panel" + panelIndex + "_" + octiconCssName;
    }

    public static String getLoginDialogOwnerFieldId() {
        return "repoOwnerField";
    }

    public static String getBoardNameInputFieldId() {
        return "boardnameinput";
    }

    public static String getBoardNameSaveButtonId() {
        return "boardsavebutton";
    }

    public static String getApiBoxId() {
        return "apiBox";
    }

    public static String getPanelCellIdForTest(int panelIndex, int issueId) {
        return "#" + getPanelCellId(panelIndex, issueId);
    }

    public static String getPanelFilterTextFieldIdForTest(int panelIndex) {
        return "#" + getPanelFilterTextFieldId(panelIndex);
    }

    public static String getPanelRenameTextFieldIdForTest(int panelIndex) {
        return "#" + getPanelRenameTextFieldId(panelIndex);
    }

    public static String getRepositorySelectorIdForTest() {
        return "#" + getRepositorySelectorId();
    }

    public static String getPanelNameAreaIdForTest(int panelIndex) {
        return "#" + getPanelNameAreaId(panelIndex);
    }

    public static String getPanelIdForTest(int panelIndex) {
        return "#" + getPanelId(panelIndex);
    }

    public static String getLabelPickerTextFieldIdForTest() {
        return "#" + getLabelPickerTextFieldId();
    }

    public static String getPanelCloseButtonIdForTest(int panelIndex) {
        return "#" + getPanelCloseButtonId(panelIndex);
    }

    public static String getPanelRenameButtonIdForTest(int panelIndex) {
        return "#" + getPanelRenameButtonId(panelIndex);
    }

    public static String getOcticonButtonIdForTest(int panelIndex, String octiconCssName) {
        return "#" + getOcticonButtonId(panelIndex, octiconCssName);
    }

    public static String getLoginDialogOwnerFieldIdForTest() {
        return "#" + getLoginDialogOwnerFieldId();
    }

    public static String getBoardNameInputFieldIdForTest() {
        return "#" + getBoardNameInputFieldId();
    }

    public static String getBoardNameSaveButtonIdForTest() {
        return "#" + getBoardNameSaveButtonId();
    }

    public static String getAssignedLabelsPaneIdForTest() {
        return "#assignedLabels";
    }

    public static String getLabelPickerQueryFieldIdForTest() {
        return "#queryField";
    }

    public static String getApiBoxIdForTest() {
        return "#" + getApiBoxId();
    }
}
