package guitests;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;

import org.junit.Test;
import org.loadui.testfx.GuiTest;

import backend.resource.TurboIssue;
import ui.IdGenerator;
import ui.UI;
import util.events.ShowLabelPickerEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LabelPickerTests extends UITest {

    private static final String ASSIGNED_LABELS_PANE_ID = IdGenerator.getAssignedLabelsPaneIdReference();

    @Test
    public void showLabelPicker_typeQuery_displaysCorrectly() {
        triggerLabelPicker(getIssueCell(0, 9).getIssue());
        clickLabelPickerTextField();
        type("world");
        assertEquals("world", getLabelPickerTextField().getText());
        exitCleanly();
    }

    @Test
    public void showLabelPicker_emptyLabels_displayedCorrectText() {
        triggerLabelPicker(new TurboIssue("dummy/dummy", 1, ""));
        waitUntilNodeAppears(ASSIGNED_LABELS_PANE_ID);
        FlowPane assignedLabels = GuiTest.find(ASSIGNED_LABELS_PANE_ID);
        assertTrue(assignedLabels.getChildren().isEmpty());
        exitCleanly();
    }

    private void exitCleanly() {
        push(KeyCode.ESCAPE);
        waitUntilNodeDisappears(IdGenerator.getLabelPickerTextFieldIdReference());
    }

    private void triggerLabelPicker(TurboIssue issue) {
        Platform.runLater(getStage()::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(issue));
        waitUntilNodeAppears(IdGenerator.getLabelPickerTextFieldIdReference());
    }
}
