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

    public static String getPanelNameAreaId(int panelIndex) {
        return "panel" + panelIndex + "_nameText";
    }

    public static String getPanelId(int panelIndex) {
        return "panel" + panelIndex;
    }

    public static String getLabelPickerTextFieldId() {
        return "labelPickerTextField";
    }

    public static String getMilestonePickerTextFieldId() {
        return "milestonePickerTextField";
    }

    public static String getAssigneePickerTextFieldId() {
        return "assigneePickerTextField";
    }

    public static String getAssigneePickerAssignedUserPaneId() {
        return "assigneePickerAssignedUserPane";
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
        return "boardNameInput";
    }

    public static String getBoardNameSaveButtonId() {
        return "boardSaveButton";
    }

    public static String getApiBoxId() {
        return "apiBox";
    }

    public static String getBoardPickerTextFieldId() {
        return "queryField";
    }

    public static String getBoardPickerSuggestedBoardListId() {
        return "boardList";
    }

    public static String getBoardPickerTextFieldReference() {
        return '#' + getBoardPickerTextFieldId();
    }

    public static String getBoardPickerSuggestedBoardListReference() {
        return '#' + getBoardPickerSuggestedBoardListId();
    }

    public static String getRepositoryPickerTextFieldId() {
        return "repositoryPickerUserInputField";
    }

    public static String getRepositoryPickerSuggestedRepoListId() {
        return "suggestedRepositoryList";
    }

    public static String getRepositoryPickerTextFieldReference() {
        return "#" + getRepositoryPickerTextFieldId();
    }

    public static String getRepositoryPickerSuggestedRepoListReference() {
        return "#" + getRepositoryPickerSuggestedRepoListId();
    }

    public static String getPanelCellIdReference(int panelIndex, int issueId) {
        return "#" + getPanelCellId(panelIndex, issueId);
    }

    public static String getPanelFilterTextFieldIdReference(int panelIndex) {
        return "#" + getPanelFilterTextFieldId(panelIndex);
    }

    public static String getPanelRenameTextFieldIdReference(int panelIndex) {
        return "#" + getPanelRenameTextFieldId(panelIndex);
    }

    public static String getPanelNameAreaIdReference(int panelIndex) {
        return "#" + getPanelNameAreaId(panelIndex);
    }

    public static String getPanelIdReference(int panelIndex) {
        return "#" + getPanelId(panelIndex);
    }

    public static String getLabelPickerTextFieldIdReference() {
        return "#" + getLabelPickerTextFieldId();
    }

    public static String getMilestonePickerTextFieldIdReference() {
        return "#" + getMilestonePickerTextFieldId();
    }

    public static String getAssigneePickerTextFieldIdReference() {
        return "#" + getAssigneePickerTextFieldId();
    }

    public static String getAssigneePickerAssignedUserPaneIdReference() {
        return "#" + getAssigneePickerAssignedUserPaneId();
    }

    public static String getPanelCloseButtonIdReference(int panelIndex) {
        return "#" + getPanelCloseButtonId(panelIndex);
    }

    public static String getPanelRenameButtonIdReference(int panelIndex) {
        return "#" + getPanelRenameButtonId(panelIndex);
    }

    public static String getOcticonButtonIdReference(int panelIndex, String octiconCssName) {
        return "#" + getOcticonButtonId(panelIndex, octiconCssName);
    }

    public static String getLoginDialogOwnerFieldIdReference() {
        return "#" + getLoginDialogOwnerFieldId();
    }

    public static String getBoardNameInputFieldIdReference() {
        return "#" + getBoardNameInputFieldId();
    }

    public static String getBoardNameSaveButtonIdReference() {
        return "#" + getBoardNameSaveButtonId();
    }

    public static String getAssignedLabelsPaneIdReference() {
        return "#assignedLabels";
    }

    public static String getLabelPickerQueryFieldIdReference() {
        return "#queryField";
    }

    public static String getApiBoxIdReference() {
        return "#" + getApiBoxId();
    }
}
