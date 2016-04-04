package guitests;

import backend.resource.TurboIssue;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.IdGenerator;
import ui.UI;
import util.events.ShowAssigneePickerEvent;

import static org.junit.Assert.assertEquals;

public class AssigneePickerTests extends UITest {

    private static final String TEXT_FIELD_ID = "#assigneePickerTextField";

    @Test
    public void showAssigneePicker_typeQuery_displaysCorrectly() {
        triggerAssigneePicker(getIssueCell(0, 9).getIssue());
        clickAssigneePickerTextField();
        selectAll();
        push(KeyCode.BACK_SPACE);
        type("world");
        assertEquals("world", getAssigneePickerTextField().getText());
        exitCleanly();
    }

    @Test
    public void showAssigneePicker_noAssignee_assigneeAssigned() {
        TurboIssue issue = getIssueCell(0, 9).getIssue();
        triggerAssigneePicker(issue);
        selectAll();
        push(KeyCode.BACK_SPACE);
        type("User");
        push(KeyCode.ENTER);
        assertEquals(true, issue.getAssignee().isPresent());
        exitCleanly();
    }

    private void exitCleanly() {
        push(KeyCode.ESCAPE);
        waitUntilNodeDisappears(IdGenerator.getLabelPickerTextFieldIdReference());
    }

    private void triggerAssigneePicker(TurboIssue issue) {
        Platform.runLater(getStage()::hide);
        UI.events.triggerEvent(new ShowAssigneePickerEvent(issue));
        waitUntilNodeAppears(TEXT_FIELD_ID);
    }

}
