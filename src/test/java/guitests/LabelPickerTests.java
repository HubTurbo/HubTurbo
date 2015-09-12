package guitests;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.controlsfx.control.NotificationPane;
import org.junit.Test;
import ui.UI;
import ui.components.FilterTextField;
import ui.listpanel.ListPanelCell;
import util.PlatformEx;
import util.events.ShowLabelPickerEvent;

import static org.junit.Assert.assertEquals;

public class LabelPickerTests extends UITest {

    @Test
    public void showLabelPickerTest() {
        FilterTextField filterTextField = find("#dummy/dummy_col0_filterTextField");
        click(filterTextField);
        type("hello");
        assertEquals("hello", filterTextField.getText());
        filterTextField.setText("");

        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        click(listPanelCell);

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        waitUntilNodeAppears("#labelPickerTextField");

        TextField labelPickerTextField = find("#labelPickerTextField");
        click(labelPickerTextField);
        type("world");
        assertEquals("world", labelPickerTextField.getText());
        push(KeyCode.ESCAPE);
        waitUntilNodeDisappears("#labelPickerTextField");
    }

    @Test
    public void addAndRemoveLabelTest() {
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        assertEquals(1, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        waitUntilNodeAppears("#labelPickerTextField");

        TextField labelPickerTextField = find("#labelPickerTextField");
        click(labelPickerTextField);
        type("2 ");
        push(KeyCode.ENTER);
        waitUntilNodeDisappears("#labelPickerTextField");
        assertEquals(0, listPanelCell.getIssueLabels().size());

        // sleep long enough for label changes to be made permanent
        NotificationPane notificationPane = find("#notificationPane");
        while (notificationPane.isShowing()) {
            PlatformEx.waitOnFxThread();
            sleep(100);
        }

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        waitUntilNodeAppears("#labelPickerTextField");

        labelPickerTextField = find("#labelPickerTextField");
        click(labelPickerTextField);
        type("2 ");
        push(KeyCode.ENTER);
        waitUntilNodeDisappears("#labelPickerTextField");
        assertEquals(1, listPanelCell.getIssueLabels().size());
    }

    @Test
    public void undoLabelChangeTest() {
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        assertEquals(1, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        waitUntilNodeAppears("#labelPickerTextField");

        TextField labelPickerTextField = find("#labelPickerTextField");
        click(labelPickerTextField);
        type("2 ");
        push(KeyCode.ENTER);
        waitUntilNodeDisappears("#labelPickerTextField");
        assertEquals(0, listPanelCell.getIssueLabels().size());

        click("Undo");
        // sleep long enough for undo
        NotificationPane notificationPane = find("#notificationPane");
        while (notificationPane.isShowing()) {
            PlatformEx.waitOnFxThread();
            sleep(100);
        }
        assertEquals(1, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        waitUntilNodeAppears("#labelPickerTextField");

        labelPickerTextField = find("#labelPickerTextField");
        click(labelPickerTextField);
        type("2 ");
        push(KeyCode.ENTER);
        waitUntilNodeDisappears("#labelPickerTextField");
        assertEquals(0, listPanelCell.getIssueLabels().size());

        click("#dummy/dummy_col0_9");
        press(KeyCode.CONTROL).press(KeyCode.Z).release(KeyCode.Z).release(KeyCode.CONTROL);
        while (notificationPane.isShowing()) {
            PlatformEx.waitOnFxThread();
            sleep(100);
        }
        assertEquals(1, listPanelCell.getIssueLabels().size());
    }

}
