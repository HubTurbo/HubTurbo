package unstable;

import guitests.UITest;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import org.controlsfx.control.NotificationPane;
import org.junit.Test;

import ui.UI;
import ui.listpanel.ListPanelCell;
import util.PlatformEx;
import util.events.ShowLabelPickerEvent;
import static org.junit.Assert.assertEquals;

public class LabelPickerTests extends UITest {

    private static final String QUERY_FIELD_ID = "#queryField";
    private static final String DEFAULT_ISSUECARD_ID = "#dummy/dummy_col0_9";
    private static final String UNDO_PANE_ID = "#notificationPane";

    @Test
    public void showLabelPicker() {
        ListPanelCell listPanelCell = find(DEFAULT_ISSUECARD_ID);
        click(listPanelCell);

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        waitUntilNodeAppears(QUERY_FIELD_ID);

        TextField labelPickerTextField = find(QUERY_FIELD_ID);
        click(labelPickerTextField);
        type("world");
        assertEquals("world", labelPickerTextField.getText());
        push(KeyCode.ESCAPE);
        waitUntilNodeDisappears(QUERY_FIELD_ID);
    }

    @Test
    public void addAndRemoveLabel_changesReflected() {
        ListPanelCell listPanelCell = triggerLabelPicker();

        TextField labelPickerTextField = find(QUERY_FIELD_ID);
        click(labelPickerTextField);
        type("2 ");
        push(KeyCode.ENTER);
        waitUntilNodeDisappears(QUERY_FIELD_ID);
        assertEquals(0, listPanelCell.getIssueLabels().size());

        // sleep long enough for label changes to be made permanent
        NotificationPane notificationPane = find(UNDO_PANE_ID);
        while (notificationPane.isShowing()) {
            PlatformEx.waitOnFxThread();
            sleep(100);
        }

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        waitUntilNodeAppears(QUERY_FIELD_ID);

        labelPickerTextField = find(QUERY_FIELD_ID);
        click(labelPickerTextField);
        type("2 ");
        push(KeyCode.ENTER);
        waitUntilNodeDisappears(QUERY_FIELD_ID);
        assertEquals(1, listPanelCell.getIssueLabels().size());
    }

    @Test
    public void undoChanges_resetAssignedLabels() {
        ListPanelCell listPanelCell = triggerLabelPicker();

        TextField labelPickerTextField = find(QUERY_FIELD_ID);
        click(labelPickerTextField);
        type("2 ");
        push(KeyCode.ENTER);
        waitUntilNodeDisappears(QUERY_FIELD_ID);
        assertEquals(2, listPanelCell.getIssueLabels().size());

        click("Undo");
        // sleep long enough for undo
        NotificationPane notificationPane = find(UNDO_PANE_ID);
        while (notificationPane.isShowing()) {
            PlatformEx.waitOnFxThread();
            sleep(100);
        }

        assertEquals(1, listPanelCell.getIssueLabels().size());
    }

    /**
     * Returns issue card used to trigger Label Picker
     */
    private ListPanelCell triggerLabelPicker() {
        ListPanelCell listPanelCell = find(DEFAULT_ISSUECARD_ID);
        assertEquals(1, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        waitUntilNodeAppears(QUERY_FIELD_ID);
        return listPanelCell;
    }
}
