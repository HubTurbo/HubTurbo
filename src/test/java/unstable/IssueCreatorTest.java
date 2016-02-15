package unstable;

import static org.junit.Assert.*;

import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import guitests.UITest;

import org.junit.Test;

import backend.resource.TurboIssue;
import ui.UI;
import ui.listpanel.ListPanelCell;
import util.events.ShowIssueCreatorEvent;

public class IssueCreatorTest extends UITest {
    
    private static final String TITLE_FIELD = "#title";
    private static final String BODY_FIELD = "#body";
    private static final String ASSIGNEE_FIELD = "#assigneeField";
    private static final String MILESTONE_FIELD = "#milestoneField";

    @Test
    public void showIssueCreator() {
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        click(listPanelCell);
        Platform.runLater(stage::hide); 

        UI.events.triggerEvent(new ShowIssueCreatorEvent(Optional.empty()));
        waitUntilNodeAppears(TITLE_FIELD);
        
        TextField title = find(TITLE_FIELD);
        verifyTextFieldInput(title, "hello");
        verifyCleanExit();
    }

    
    @Test
    public void verifyEditIssueContent() {
        // Ensures the content and title matches issue being edited
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        TurboIssue issue = listPanelCell.getIssue();
        String assignee = "dummy";
        int milestone = 0;
        issue.setAssignee(assignee);
        issue.setMilestone(milestone);

        UI.events.triggerEvent(new ShowIssueCreatorEvent(Optional.of(issue)));
        waitUntilNodeAppears(TITLE_FIELD);
        
        TextField title = find(TITLE_FIELD);
        TextField assigneeField = find(ASSIGNEE_FIELD);
        TextField milestoneField = find(MILESTONE_FIELD);

        assertEquals(issue.getTitle(), title.getText());
        assertEquals(assignee, assigneeField.getText());
        assertEquals(milestone, Integer.parseInt(milestoneField.getText()));
        verifyCleanExit();
    }

    /**
     * Validates input to text field. Test fails when content does not match input.
     */
    private void verifyTextFieldInput(TextField field, String content) {
        click(field);
        type(content);
        
        assertEquals(content, field.getText());
    }
    
    private void verifyCleanExit() {
        push(KeyCode.ESCAPE);
        waitUntilNodeDisappears(TITLE_FIELD);
    }
}
