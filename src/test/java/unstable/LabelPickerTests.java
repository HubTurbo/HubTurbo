package unstable;

import guitests.UITest;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;

import org.junit.Test;

import backend.resource.TurboIssue;
import ui.UI;
import ui.listpanel.ListPanelCell;
import util.events.ShowLabelPickerEvent;
import static org.junit.Assert.assertEquals;

public class LabelPickerTests extends UITest {

    private static final String QUERY_FIELD_ID = "#queryField";
    private static final String DEFAULT_ISSUECARD_ID = "#dummy/dummy_col0_9";

    @Test
    public void showLabelPicker_typeQuery_displaysCorrectly() {
        triggerLabelPicker(getIssueCard(DEFAULT_ISSUECARD_ID).getIssue());
        TextField labelPickerTextField = find(QUERY_FIELD_ID);
        click(labelPickerTextField);
        type("world");
        assertEquals("world", labelPickerTextField.getText());
        exitCleanly();
    }

    @Test
    public void showLabelPicker_emptyLabels_displayedCorrectText() {
        triggerLabelPicker(new TurboIssue("dummy/dummy", 1, ""));
        waitUntilNodeAppears("#assignedLabels");
        FlowPane assignedLabels = find("#assignedLabels");
        Label label = (Label) assignedLabels.getChildren().get(0);
        assertEquals("No currently selected labels. ", label.getText());
        exitCleanly();
    }

    private void exitCleanly() {
        push(KeyCode.ESCAPE);
        waitUntilNodeDisappears(QUERY_FIELD_ID);
    }

    private ListPanelCell getIssueCard(String issueCardId) {
        return find(issueCardId);
    }

    private void triggerLabelPicker(TurboIssue issue) {
        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowLabelPickerEvent(issue));
        waitUntilNodeAppears(QUERY_FIELD_ID);
    }
}
