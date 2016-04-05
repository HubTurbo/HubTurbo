package guitests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.loadui.testfx.GuiTest;

import backend.resource.TurboIssue;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import ui.UI;
import util.events.ShowIssuePickerEvent;

public class IssuePickerTest extends UITest {

    private static final String QUERY_FIELD_ID = "#issuepickerQueryField";

    @Test
    public void showIssuePicker_typeQuery_displaysCorrectly() {
        triggerIssuePicker(new ArrayList<>());
        TextField issuePickerTextField = GuiTest.find(QUERY_FIELD_ID);
        clickOn(issuePickerTextField);
        type("world");
        assertEquals("world", issuePickerTextField.getText());
    }

    private void triggerIssuePicker(List<TurboIssue> allIssues) {
        Platform.runLater(getStage()::hide);
        UI.events.triggerEvent(new ShowIssuePickerEvent(allIssues, false));
        waitUntilNodeAppears(QUERY_FIELD_ID);
    }
}
