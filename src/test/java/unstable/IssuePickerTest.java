package unstable;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import backend.resource.TurboIssue;
import guitests.UITest;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import ui.UI;
import util.events.ShowIssuePickerEvent;

public class IssuePickerTest extends UITest {

    private static final String QUERY_FIELD_ID = "#issuepickerQueryField";

    @Test
    public void showIssuePicker_typeQuery_displaysCorrectly() {
        triggerIssuePicker(new ArrayList<>());
        TextField issuePickerTextField = find(QUERY_FIELD_ID);
        click(issuePickerTextField);
        type("world");
        assertEquals("world", issuePickerTextField.getText());
        exitCleanly();
    }

    private void exitCleanly() {
        push(KeyCode.ESCAPE);
        waitUntilNodeDisappears(QUERY_FIELD_ID);
    }

    private void triggerIssuePicker(List<TurboIssue> allIssues) {
        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowIssuePickerEvent(allIssues, false));
        waitUntilNodeAppears(QUERY_FIELD_ID);
    }
}
