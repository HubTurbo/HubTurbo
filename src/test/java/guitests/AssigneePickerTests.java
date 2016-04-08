package guitests;

import backend.resource.TurboIssue;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Test;
import ui.IdGenerator;
import ui.UI;
import util.events.ShowAssigneePickerEvent;

import static org.junit.Assert.assertEquals;

public class AssigneePickerTests extends UITest {

    @Test
    public void showAssigneePicker_typeQuery_displaysCorrectly() {
        triggerAssigneePicker(getIssueCell(0, 9).getIssue());
        clickAssigneePickerTextField();
        type("world");
        assertEquals("world", getAssigneePickerTextField().getText());
    }

    @Test
    public void showAssigneePicker_pickAUser_userPickedAssigned() {
        TurboIssue issue = getIssueCell(0, 9).getIssue();
        triggerAssigneePicker(issue);
        type("User 1");
        push(KeyCode.ENTER);
        assertEquals("User 1", issue.getAssignee().get());
    }

    @Test
    public void showAssigneePicker_pressEnterAfterAssigneePickerShown_existingAssigneeUnassigned() {
        TurboIssue issue = getIssueCell(0, 9).getIssue();
        assertEquals(true, issue.getAssignee().isPresent());
        triggerAssigneePicker(issue);
        push(KeyCode.ENTER);
        assertEquals(false, issue.getAssignee().isPresent());
    }

    @Test
    public void showAssigneePicker_pressEscAfterAssigneePickerShown_existingAssigneeUnchanged() {
        TurboIssue issue = getIssueCell(0, 9).getIssue();
        String existingAssignee = issue.getAssignee().get();
        triggerAssigneePicker(issue);
        push(KeyCode.ESCAPE);
        assertEquals(existingAssignee, issue.getAssignee().get());
    }

    @Test
    public void showAssigneePicker_pickExistingAssignee_existingAssigneeWillNotBeANewlyAddedAssignee() {
        triggerAssigneePicker(getIssueCell(0, 9).getIssue());
        clickAssigneePickerTextField();
        type("User 9");
        assertEquals(2, getAssigneePickerAssignedUserPane().getChildren().size());
    }

    @After
    public void exitCleanly() {
        push(KeyCode.ESCAPE);
        waitUntilNodeDisappears(IdGenerator.getAssignedLabelsPaneIdReference());
    }

    private void triggerAssigneePicker(TurboIssue issue) {
        Platform.runLater(getStage()::hide);
        UI.events.triggerEvent(new ShowAssigneePickerEvent(issue));
        waitUntilNodeAppears(IdGenerator.getAssigneePickerTextFieldIdReference());
    }

}
