package unstable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ui.components.KeyboardShortcuts.JUMP_TO_FIRST_ISSUE;

import github.IssueEventType;
import github.TurboIssueEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import guitests.UITest;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.User;
import org.junit.Test;

import org.loadui.testfx.GuiTest;
import org.loadui.testfx.exceptions.NoNodesFoundException;
import tests.TurboIssueEventTests;
import ui.GuiElement;
import ui.UI;
import ui.listpanel.ListPanel;
import ui.listpanel.ListPanelCard;
import util.PlatformEx;
import util.Utility;
import util.events.testevents.UILogicRefreshEvent;
import util.events.testevents.UpdateDummyRepoEvent;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;

public class IssuePanelTests extends UITest {

    private static final int EVENT_DELAY = 1000;

    @Test
    public void keepSelectionTest() {
        // checks to see if ListPanel keeps the same issue selected even after
        // the list is updated
        ListPanel issuePanel = find("#dummy/dummy_col0");
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("sort:date");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        press(JUMP_TO_FIRST_ISSUE);
        push(KeyCode.DOWN).push(KeyCode.DOWN);
        sleep(EVENT_DELAY);
        assertEquals(true, issuePanel.getSelectedElement().isPresent());
        assertEquals(3, issuePanel.getSelectedElement().get().getIssue().getId());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(UpdateDummyRepoEvent.updateIssue("dummy/dummy", 3, "updated issue"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        assertEquals(true, issuePanel.getSelectedElement().isPresent());
        assertEquals(3, issuePanel.getSelectedElement().get().getIssue().getId());
    }

    @Test
    public void guiElementsTest() {
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("id:8");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        // Issue #8 was assigned label 11, but it was removed
        try {
            GuiTest.exists("Label 11");
            fail();
        } catch (NoNodesFoundException e) { /* Successful, we should not be able to see label 11 */ }

        type(" updated:5");
        push(KeyCode.ENTER);
        // After we load the metadata, label 11 should appear.
        waitUntilNodeAppears("Label 11");
        // Ensure that the "Label 11" text found represents the label from backend
        assertEquals(true, find("Label 11").getStyle().contains("-fx-background-color: #ffa500"));

        // Next we check for a label that was deleted from the repository, but should still be displayed under
        // metadata (label update events).
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("id:9 updated:5");
        PlatformEx.waitOnFxThread();
        push(KeyCode.ENTER);
        waitUntilNodeAppears("Deleted");
        // We should see the "Deleted" label with the proper color despite the label having been deleted.
        assertEquals(true, find("Deleted").getStyle().contains("-fx-background-color: #84b6eb"));
    }

    @Test
    public void testCreateLabelUpdateEventNodesForNoEvent() {
        GuiElement guiElement = new GuiElement(
                new TurboIssue("test/test", 1, "Test issue"),
                new ArrayList<>(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());

        assertEquals(0,
                TurboIssueEvent.createLabelUpdateEventNodes(
                        guiElement, new ArrayList<>()).size());
    }

    @Test
    public void testCreateLabelUpdateEventNodesForSampleEvents()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
                   NoSuchMethodException, SecurityException {

        Method layoutMethod = ListPanelCard.class.getDeclaredMethod(
                "layoutEvents", GuiElement.class, List.class, List.class);
        layoutMethod.setAccessible(true);

        List<TurboLabel> labels = new ArrayList<>();
        labels.add(new TurboLabel("test/test", "aaaaaa", "A1"));
        labels.add(new TurboLabel("test/test", "aaaaaa", "A2"));
        labels.add(new TurboLabel("test/test", "bbbbbb", "B1"));
        labels.add(new TurboLabel("test/test", "bbbbbb", "B2"));
        labels.add(new TurboLabel("test/test", "cccccc", "C1"));
        labels.add(new TurboLabel("test/test", "dddddd", "D1"));
        GuiElement guiElement = new GuiElement(
                new TurboIssue("test/test", 1, "issue"),
                labels,
                Optional.empty(),
                Optional.empty(),
                Optional.empty());

        List<TurboIssueEvent> events =
                new ArrayList<>(new TurboIssueEventTests().sampleEvents);
        List<Node> nodes = TurboIssueEvent.createLabelUpdateEventNodes(guiElement, events);

        assertEquals(5, TurboIssueEvent.createLabelUpdateEventNodes(guiElement, events).size());
        assertEquals(5, ((HBox) nodes.get(0)).getChildren().size());
        assertEquals(5, ((HBox) nodes.get(1)).getChildren().size());
        assertEquals(4, ((HBox) nodes.get(2)).getChildren().size());
        assertEquals(4, ((HBox) nodes.get(3)).getChildren().size());
        assertEquals(4, ((HBox) nodes.get(4)).getChildren().size());
        assertEquals(5, ((VBox) layoutMethod.invoke(null,
                                    guiElement, events, new ArrayList<Comment>())).getChildren().size());
    }

    @Test
    public void testCreateLabelUpdateEventNodesForNonExistentLabel() {
        GuiElement guiElement = new GuiElement(
                new TurboIssue("test/test", 1, "Test issue"),
                new ArrayList<>(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());

        List<TurboIssueEvent> events = new ArrayList<>();
        events.add(
                new TurboIssueEvent(
                    new User().setLogin("A"), IssueEventType.Labeled,
                        Utility.localDateTimeToDate(LocalDateTime.of(2015, 1, 1, 1, 1, 0)))
                    .setLabelName("X").setLabelColour("ffffff"));

        assertEquals(1,
                TurboIssueEvent.createLabelUpdateEventNodes(
                        guiElement, events).size());
    }

    @Test
    public void showAuthorAssignee_assignedPullRequest_authorAssigneeShown() {
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("id:11");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(true, GuiTest.exists("User 11"));
        // arrow to indicate assignment i.e. author -> assignee
        assertEquals(true, GuiTest.exists("\uf03e"));
        assertEquals(true, GuiTest.exists("User 1"));
    }

    @Test
    public void showAuthorAssignee_assignedIssue_onlyAssigneeShown() {
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("id:12");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        // author should not show since it is an issue
        try {
            GuiTest.exists("User 12");
            fail();
        } catch (NoNodesFoundException e) { /* Successful since it should not show */ }
        // assignee should still show
        assertEquals(true, GuiTest.exists("User 2"));
    }
}
