package unstable;

import guitests.UITest;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import org.controlsfx.control.NotificationPane;
import org.junit.Ignore;
import org.junit.Test;

import ui.UI;
import ui.components.FilterTextField;
import ui.listpanel.ListPanelCell;
import util.PlatformEx;
import util.events.ShowLabelPickerEvent;
import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.UNDO_LABEL_CHANGES;

public class LabelPickerTests extends UITest {

    private static final String textFieldId = "#queryField";

    @Ignore
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
        waitUntilNodeAppears(textFieldId);

        TextField labelPickerTextField = find(textFieldId);
        click(labelPickerTextField);
        type("world");
        assertEquals("world", labelPickerTextField.getText());
        push(KeyCode.ESCAPE);
        waitUntilNodeDisappears(textFieldId);
    }

    @Ignore
    @Test
    public void addAndRemoveLabelTest() {
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        assertEquals(1, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        waitUntilNodeAppears(textFieldId);

        TextField labelPickerTextField = find(textFieldId);
        click(labelPickerTextField);
        type("2 ");
        push(KeyCode.ENTER);
        waitUntilNodeDisappears(textFieldId);
        assertEquals(0, listPanelCell.getIssueLabels().size());

        // sleep long enough for label changes to be made permanent
        NotificationPane notificationPane = find("#notificationPane");
        while (notificationPane.isShowing()) {
            PlatformEx.waitOnFxThread();
            sleep(100);
        }

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        waitUntilNodeAppears(textFieldId);

        labelPickerTextField = find(textFieldId);
        click(labelPickerTextField);
        type("2 ");
        push(KeyCode.ENTER);
        waitUntilNodeDisappears(textFieldId);
        assertEquals(1, listPanelCell.getIssueLabels().size());
    }

    @Ignore
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
        press(UNDO_LABEL_CHANGES);
        while (notificationPane.isShowing()) {
            PlatformEx.waitOnFxThread();
            sleep(100);
        }
        assertEquals(1, listPanelCell.getIssueLabels().size());
    }

//    @Test
//    public void rapidLabelChangeTest() {
//        resetDummyRepo();
//        ListPanelCell issue9_cell = find("#dummy/dummy_col0_9");
//        assertEquals(1, issue9_cell.getIssueLabels().size());
//
//        Platform.runLater(stage::hide);
//        UI.events.triggerEvent(new ShowLabelPickerEvent(issue9_cell.getIssue()));
//        waitUntilNodeAppears("#labelPickerTextField");
//
//        TextField labelPickerTextField = find("#labelPickerTextField");
//        click(labelPickerTextField);
//        logAndType("2 ");
//        System.out.println(stage);
//        closeLabelPickerWithEnter();
//        assertEquals(0, issue9_cell.getIssueLabels().size());
//
//        ListPanelCell issue8_cell = find("#dummy/dummy_col0_8");
//        assertEquals(1, issue8_cell.getIssueLabels().size());
//
//        Platform.runLater(stage::hide);
//        UI.events.triggerEvent(new ShowLabelPickerEvent(issue8_cell.getIssue()));
//        waitUntilNodeAppears("#labelPickerTextField");
//
//        labelPickerTextField = find("#labelPickerTextField");
//        click(labelPickerTextField);
//        logAndType("2 ");
//        closeLabelPickerWithEnter();
//        assertEquals(2, issue8_cell.getIssueLabels().size());
//
//        // force dummy repo to update that particular issue
//        UI.events.triggerEvent(UpdateDummyRepoEvent.updateIssue("dummy/dummy", 9, "Issue 9"));
//        uiRefresh();
//
//        issue9_cell = find("#dummy/dummy_col0_9");
//        assertEquals(0, issue9_cell.getIssueLabels().size());
//    }
//
//    private void resetDummyRepo() {
//        logger.info("Resetting dummy repo");
//        UI.events.triggerEvent(UpdateDummyRepoEvent.resetRepo("dummy/dummy"));
//        uiRefresh();
//        logger.info("Dummy repo reset");
//    }
//
//    private void uiRefresh() {
//        logger.info("Refreshing UI");
//        waitUntilNodeAppears("#dummy/dummy_col0_9");
//        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
//        String updatedTime = listPanelCell.getIssue().getUpdatedAt().toString();
//        UI.events.triggerEvent(new UILogicRefreshEvent());
//        String newTime = updatedTime;
//        while (newTime.equals(updatedTime)) {
//            sleep(100);
//            waitUntilNodeAppears("#dummy/dummy_col0_9");
//            ListPanelCell newListPanelCell = find("#dummy/dummy_col0_9");
//            newTime = newListPanelCell.getIssue().getUpdatedAt().toString();
//        }
//        logger.info("UI refreshed");
//    }
//
//    private void closeLabelPickerWithEnter() {
//        logger.info("Closing label picker with ENTER");
//        try {
//            while (existsQuiet("#labelPickerTextField")) {
//                logger.info("Trying to close label picker");
//                click("#labelPickerTextField");
//                push(KeyCode.ENTER);
//                sleep(100);
//            }
//        } catch (NoNodesFoundException ignored) {}
//        logger.info("Label picker closed");
//    }
//
//    private void closeLabelPickerWithEscape() {
//        logger.info("Closing label picker with ESCAPE");
//        try {
//            while (existsQuiet("#labelPickerTextField")) {
//                logger.info("Trying to close label picker");
//                click("#labelPickerTextField");
//                sleep(100);
//                push(KeyCode.ESCAPE);
//            }
//        } catch (NoNodesFoundException ignored) {}
//        logger.info("Label picker closed");
//    }

}
