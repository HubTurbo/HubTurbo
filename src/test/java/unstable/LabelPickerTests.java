package unstable;

import guitests.UITest;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;

import org.junit.Test;

import backend.resource.TurboIssue;
import ui.IdGenerator;
import ui.UI;
import util.events.ShowLabelPickerEvent;

import static org.junit.Assert.assertEquals;

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
        FlowPane assignedLabels = find(ASSIGNED_LABELS_PANE_ID);
        Label label = (Label) assignedLabels.getChildren().get(0);
        assertEquals("No currently selected labels. ", label.getText());
        exitCleanly();
    }

    private void exitCleanly() {
        push(KeyCode.ESCAPE);
        waitUntilNodeDisappears(IdGenerator.getLabelPickerTextFieldIdReference());
    }

    private void triggerLabelPicker(TurboIssue issue) {
        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(issue));
        waitUntilNodeAppears(IdGenerator.getLabelPickerTextFieldIdReference());
    }
}
