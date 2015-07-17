package guitests;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.listpanel.ListPanelCell;
import util.events.ShowLabelPickerEvent;

import static org.junit.Assert.assertEquals;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.controls.Commons.hasText;

public class LabelPickerTests extends UITest {

    private static final int SHOW_DIALOG_DELAY = 2000;
    public static final int EVENT_DELAY = 500;

    @Test
    public void showLabelPickerTest() {
        click("#dummy/dummy_col0_9");
        push(KeyCode.L);
        sleep(EVENT_DELAY);
        assertNodeExists(hasText("Issue #9: Issue 9"));
        push(KeyCode.ENTER);
    }

    @Test
    public void addAndRemoveLabelTest() {
        click("#dummy/dummy_col0_filterTextField");

        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        click(listPanelCell);
        assertEquals(1, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        sleep(SHOW_DIALOG_DELAY);

        TextField labelPickerTextField = find("#labelPickerTextField");
        click(labelPickerTextField);
        type("3 ");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        click("#dummy/dummy_col0_filterTextField");
        assertEquals(2, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        sleep(SHOW_DIALOG_DELAY);

        click(labelPickerTextField);
        type("3 ");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        click("#dummy/dummy_col0_filterTextField");
        assertEquals(1, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        sleep(SHOW_DIALOG_DELAY);

        click(labelPickerTextField);
        type("2 ");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        click("#dummy/dummy_col0_filterTextField");
        assertEquals(0, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        sleep(SHOW_DIALOG_DELAY);

        click(labelPickerTextField);
        type("2 ");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        click("#dummy/dummy_col0_filterTextField");
        assertEquals(1, listPanelCell.getIssueLabels().size());
    }

    @Test
    public void moveHighlightTest() {
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        click(listPanelCell);
        assertEquals(1, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        sleep(SHOW_DIALOG_DELAY);

        type("1");
        push(KeyCode.DOWN);
        push(KeyCode.UP);
        push(KeyCode.DOWN);
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(true, listPanelCell.getIssueLabels().contains("Label 10"));

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        sleep(SHOW_DIALOG_DELAY);

        type("10 ");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(1, listPanelCell.getIssueLabels().size());
    }

}
