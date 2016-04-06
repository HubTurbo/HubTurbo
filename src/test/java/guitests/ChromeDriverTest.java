package guitests;

import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxToolkit;

import ui.TestController;
import ui.UI;
import util.GitHubURL;
import util.PlatformEx;
import util.events.IssueCreatedEvent;
import util.events.IssueSelectedEvent;
import util.events.LabelCreatedEvent;
import util.events.MilestoneCreatedEvent;
import util.events.testevents.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.concurrent.TimeoutException;

public class ChromeDriverTest extends UITest {

    private final List<String> urlsNavigated = new ArrayList<>();
    private final List<String> scriptsExecuted = new ArrayList<>();
    private final List<String> keysSentToBrowser = new ArrayList<>();
    private boolean hasJumpedToComment = false;

    private final NavigateToPageEventHandler navToPageHandler = e -> urlsNavigated.add(e.url);
    private final ExecuteScriptEventHandler execScriptHandler = e -> scriptsExecuted.add(e.script);
    private final JumpToNewCommentBoxEventHandler jumpCommentHandler = e -> hasJumpedToComment = true;

    @Before
    public void prepare() {
        urlsNavigated.clear();
        scriptsExecuted.clear();
        keysSentToBrowser.clear();
        hasJumpedToComment = false;
        UI.events.registerEvent(navToPageHandler);
        UI.events.registerEvent(execScriptHandler);
        UI.events.registerEvent(jumpCommentHandler);
        PlatformEx.runAndWait(()->TestController.getUI().getPanelControl().getPanel(0).requestFocus());
    }

    @After
    public void tearDown() {
        UI.events.unregisterEvent(navToPageHandler);
        UI.events.unregisterEvent(execScriptHandler);
        UI.events.unregisterEvent(jumpCommentHandler);
    }

    @Override
    public void setup() throws TimeoutException {
        FxToolkit.setupApplication(
            TestUI.class, "--test=true", "--bypasslogin=true", "--testchromedriver=true");
    }
    
    /**
     * Performs the specified action and waits for a specific response to occur.
     *
     * @param expected expected response to the action.
     * @param action the action to take to trigger the desired response.
     * @param responses aggregator of responses to the action.
     */
    private <T> void actAndAwaitResponse(T expected, Runnable action, Collection<T> responses) {
        responses.clear();
        action.run();
        awaitCondition(() -> responses.contains(expected));
    }
    
    private void actAndAwaitNavToPage(String expectedUrl, Runnable action) {
        actAndAwaitResponse(expectedUrl, action, urlsNavigated);
    }

    @Test
    public void navigateToPage_selectIssue() {
        actAndAwaitNavToPage(
            GitHubURL.getPathForIssue("dummy/dummy", 1),
            () -> UI.events.triggerEvent(new IssueSelectedEvent("dummy/dummy", 1, 0, false))
        );
    }

    @Test
    public void navigateToPage_createIssue() {
        actAndAwaitNavToPage(
            GitHubURL.getPathForNewIssue("dummy/dummy"),
            () -> UI.events.triggerEvent(new IssueCreatedEvent())
        );
    }

    @Test
    public void navigateToPage_createLabel() {
        actAndAwaitNavToPage(
            GitHubURL.getPathForNewLabel("dummy/dummy"),
            () -> UI.events.triggerEvent(new LabelCreatedEvent())
        );
        actAndAwaitNavToPage(
            GitHubURL.getPathForNewLabel("dummy/dummy"),
            () -> push(KeyCode.G).push(KeyCode.L)
        );
    }

    @Test
    public void navigateToPage_createMilestone() {
        actAndAwaitNavToPage(
            GitHubURL.getPathForNewMilestone("dummy/dummy"),
            () -> UI.events.triggerEvent(new MilestoneCreatedEvent())
        );
    }

    @Test
    public void navigateToPage_showDocs() {
        actAndAwaitNavToPage(
            GitHubURL.DOCS_PAGE,
            () -> push(KeyCode.F1)
        );
        actAndAwaitNavToPage(
            GitHubURL.DOCS_PAGE,
            () -> {
                push(KeyCode.G);
                push(KeyCode.H);
            }
        );
        actAndAwaitNavToPage(
            GitHubURL.DOCS_PAGE,
            () -> {
                clickOn("View");
                clickOn("Documentation");
            }
        );
    }

    @Test
    public void navigateToPage_issues() {
        // go to issues page
        actAndAwaitNavToPage(
            GitHubURL.getPathForAllIssues("dummy/dummy"),
            () -> push(KeyCode.G).push(KeyCode.I)
        );
    }

    @Test
    public void navigateToPage_milestones() {
        actAndAwaitNavToPage(
            GitHubURL.getPathForMilestones("dummy/dummy"),
            () -> push(KeyCode.G).push(KeyCode.M)
        );
    }

    @Test
    public void navigateToPage_pullRequests() {
        actAndAwaitNavToPage(
            GitHubURL.getPathForPullRequests("dummy/dummy"),
            () -> push(KeyCode.G).push(KeyCode.P)
        );
    }

    @Test
    public void navigateToPage_developers() {
        actAndAwaitNavToPage(
            GitHubURL.getPathForContributors("dummy/dummy"),
            () -> push(KeyCode.G).push(KeyCode.D)
        );
    }

    @Test
    public void navigateToPage_keyboardShortcuts() {
        actAndAwaitNavToPage(
            GitHubURL.KEYBOARD_SHORTCUTS_PAGE,
            () -> push(KeyCode.G).push(KeyCode.K)
        );
    }

    private void actAndAwaitExecScript(String expectedScript, Runnable action) {
        actAndAwaitResponse(expectedScript, action, scriptsExecuted);
    }
    
    @Test
    public void executeScripts_scrollToTop() {
        actAndAwaitExecScript(
            "window.scrollTo(0, 0)",
            () -> push(KeyCode.I)
        );
    }

    @Test
    public void executeScripts_scrollToBottom() {
        actAndAwaitExecScript(
            "window.scrollTo(0, document.body.scrollHeight)",
            () -> push(KeyCode.N)
        );
    }

    @Test
    public void executeScripts_scrollUp() {
        actAndAwaitExecScript(
            "window.scrollBy(0, -100)",
            () -> push(KeyCode.J)
        );
    }

    @Test
    public void executeScripts_scrollDown() {
        actAndAwaitExecScript(
            "window.scrollBy(0, 100)",
            () -> push(KeyCode.K)
        );
    }

    @Test
    public void jumpToNewCommentBox() {
        // jump to comments
        push(KeyCode.R, 1);
        awaitCondition(() -> hasJumpedToComment);
    }

}
