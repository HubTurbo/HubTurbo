package unstable;

import static org.junit.Assert.*;

import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import guitests.UITest;

import org.junit.Test;

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
        
        push(KeyCode.ESCAPE);
        waitUntilNodeDisappears(TITLE_FIELD);
    }

    
    /**
     * Validates input to text field. Test fails when content does not match input.
     */
    private void verifyTextFieldInput(TextField field, String content) {
        click(field);
        type(content);
        
        assertEquals(content, field.getText());
    }
}
