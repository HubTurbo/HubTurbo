package guitests;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.components.FilterTextField;
import ui.listpanel.ListPanelCell;
import util.events.ShowLabelPickerEvent;

import static org.junit.Assert.assertEquals;

public class LabelPickerTests extends UITest {

    private static final int DIALOG_DELAY = 1500;
    public static final int EVENT_DELAY = 1000;

    @Test
    public void showLabelPickerTest() {
        FilterTextField filterTextField = find("#dummy/dummy_col0_filterTextField");
        click(filterTextField);
        type("hello");
        sleep(EVENT_DELAY);
        assertEquals("hello", filterTextField.getText());
        filterTextField.setText("");

        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        click(listPanelCell);

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        sleep(DIALOG_DELAY);

        TextField labelPickerTextField = find("#labelPickerTextField");
        click(labelPickerTextField);
        type("world");
        sleep(EVENT_DELAY);
        assertEquals("world", labelPickerTextField.getText());
        push(KeyCode.ESCAPE);
        sleep(EVENT_DELAY);
    }

    @Test
    public void addAndRemoveLabelTest() {
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        assertEquals(1, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        sleep(DIALOG_DELAY);

        type("2 ");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(0, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        sleep(DIALOG_DELAY);

        type("2 ");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(1, listPanelCell.getIssueLabels().size());
    }

    @Test
    public void moveHighlightTest() {
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        assertEquals(1, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        sleep(DIALOG_DELAY);

        type("1");
        sleep(EVENT_DELAY);
        push(KeyCode.DOWN);
        sleep(EVENT_DELAY);
        push(KeyCode.UP);
        sleep(EVENT_DELAY);
        push(KeyCode.DOWN);
        sleep(EVENT_DELAY);
        push(KeyCode.ENTER);
        sleep(DIALOG_DELAY);
        assertEquals(true, listPanelCell.getIssueLabels().contains("Label 10"));

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        sleep(DIALOG_DELAY);

        type("10");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(1, listPanelCell.getIssueLabels().size());
    }

    @Test
    public void groupTest() {
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        assertEquals(1, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        sleep(DIALOG_DELAY);

        type("t.s");
        sleep(EVENT_DELAY);
        push(KeyCode.ENTER);
        sleep(DIALOG_DELAY);
        assertEquals(true, listPanelCell.getIssueLabels().contains("type.story"));

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        sleep(DIALOG_DELAY);

        type("t.r");
        sleep(EVENT_DELAY);
        push(KeyCode.ENTER);
        sleep(DIALOG_DELAY);
        assertEquals(true, listPanelCell.getIssueLabels().contains("type.research"));
        assertEquals(2, listPanelCell.getIssueLabels().size());

        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(listPanelCell.getIssue()));
        sleep(DIALOG_DELAY);

        type("t.r");
        sleep(EVENT_DELAY);
        push(KeyCode.ENTER);
        sleep(DIALOG_DELAY);
        assertEquals(1, listPanelCell.getIssueLabels().size());
    }

}
