package unstable;

import backend.resource.TurboIssue;
import guitests.UITest;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.listpanel.ListPanelCell;
import util.events.ShowAssigneePickerEvent;

import static org.junit.Assert.assertEquals;

public class AssigneePickerTests extends UITest {

    private static final String TEXT_FIELD_ID = "#assigneePickerTextField";
    private static final String DEFAULT_ISSUECARD_ID = "#dummy/dummy_col0_9";

    @Test
    public void showAssigneePicker_typeQuery_displaysCorrectly() {
        triggerAssigneePicker(getIssueCard(DEFAULT_ISSUECARD_ID).getIssue());
        TextField assigneePickerTextField = find(TEXT_FIELD_ID);
        type("world");
        assertEquals("world", assigneePickerTextField.getText());
    }

    @Test
    public void showAssigneePicker_noAssignee_assigneeAssigned() {
        TurboIssue issue = getIssueCard(DEFAULT_ISSUECARD_ID).getIssue();
        triggerAssigneePicker(issue);
        type("User");
        push(KeyCode.ENTER);
        assertEquals(true, issue.getAssignee().isPresent());
    }


    private void triggerAssigneePicker(TurboIssue issue) {
        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowAssigneePickerEvent(issue));
        waitUntilNodeAppears(TEXT_FIELD_ID);
    }

    private ListPanelCell getIssueCard(String issueCardId) {
        return find(issueCardId);
    }

}
