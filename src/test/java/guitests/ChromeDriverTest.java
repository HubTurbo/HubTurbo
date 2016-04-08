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
import java.util.List;

import java.util.concurrent.TimeoutException;

public class ChromeDriverTest extends UITest {

    private final List<String> urlsNavigated = new ArrayList<>();
    private final List<String> scriptsExecuted = new ArrayList<>();
    private boolean hasJumpedToComment = false;

    private final NavigateToPageEventHandler navToPageHandler = e -> urlsNavigated.add(e.url);
    private final ExecuteScriptEventHandler execScriptHandler = e -> scriptsExecuted.add(e.script);
    private final JumpToNewCommentBoxEventHandler jumpCommentHandler = e -> hasJumpedToComment = true;

    @Before
    public void prepare() {
        urlsNavigated.clear();
        scriptsExecuted.clear();
        hasJumpedToComment = false;
        UI.events.registerEvent(navToPageHandler);
        UI.events.registerEvent(execScriptHandler);
        UI.events.registerEvent(jumpCommentHandler);
        PlatformEx.runLaterAndWait(()->TestController.getUI().getPanelControl().getPanel(0).requestFocus());
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

    @Test
    public void navigateToPage_selectIssue() {
        UI.events.triggerEvent(new IssueSelectedEvent("dummy/dummy", 1, 0, false));
        awaitCondition(() -> urlsNavigated.contains(GitHubURL.getPathForIssue("dummy/dummy", 1)));
    }

    @Test
    public void navigateToPage_createIssue() {
        UI.events.triggerEvent(new IssueCreatedEvent());
        awaitCondition(() -> urlsNavigated.contains(GitHubURL.getPathForNewIssue("dummy/dummy")));
    }

    @Test
    public void navigateToPage_newLabelKey_createLabel() {
        push(KeyCode.G).push(KeyCode.L);
        awaitCondition(() -> urlsNavigated.contains(GitHubURL.getPathForNewLabel("dummy/dummy")));
    }
    
    @Test
    public void navigateToPage_triggerEvent_createLabel() {
        UI.events.triggerEvent(new LabelCreatedEvent());
        awaitCondition(() -> urlsNavigated.contains(GitHubURL.getPathForNewLabel("dummy/dummy")));
    }

    @Test
    public void navigateToPage_createMilestone() {
        UI.events.triggerEvent(new MilestoneCreatedEvent());
        awaitCondition(() -> urlsNavigated.contains(GitHubURL.getPathForNewMilestone("dummy/dummy")));
    }

    @Test
    public void navigateToPage_showDocsKey_showDocs() {
        push(KeyCode.F1);
        awaitCondition(() -> urlsNavigated.contains(GitHubURL.DOCS_PAGE));
    }

    @Test
    public void navigateToPage_helpKey_showDocs() {
        push(KeyCode.G).push(KeyCode.H);
        awaitCondition(() -> urlsNavigated.contains(GitHubURL.DOCS_PAGE));
    }

    @Test
    public void navigateToPage_dropdown_showDocs() {
        clickOn("View");
        clickOn("Documentation");
        awaitCondition(() -> urlsNavigated.contains(GitHubURL.DOCS_PAGE));
    }

    @Test
    public void navigateToPage_issues() {
        // go to issues page
        push(KeyCode.G).push(KeyCode.I);
        awaitCondition(() -> urlsNavigated.contains(GitHubURL.getPathForAllIssues("dummy/dummy")));
    }

    @Test
    public void navigateToPage_milestones() {
        push(KeyCode.G).push(KeyCode.M);
        awaitCondition(() -> urlsNavigated.contains(GitHubURL.getPathForMilestones("dummy/dummy")));
    }

    @Test
    public void navigateToPage_pullRequests() {
        push(KeyCode.G).push(KeyCode.P);
        awaitCondition(() -> urlsNavigated.contains(GitHubURL.getPathForPullRequests("dummy/dummy")));
    }

    @Test
    public void navigateToPage_developers() {
        push(KeyCode.G).push(KeyCode.D);
        awaitCondition(() -> urlsNavigated.contains(GitHubURL.getPathForContributors("dummy/dummy")));
    }

    @Test
    public void navigateToPage_keyboardShortcuts() {
        push(KeyCode.G).push(KeyCode.K);
        awaitCondition(() -> urlsNavigated.contains(GitHubURL.KEYBOARD_SHORTCUTS_PAGE));
    }

    @Test
    public void executeScripts_scrollToTop() {
        push(KeyCode.I);
        awaitCondition(() -> scriptsExecuted.contains("window.scrollTo(0, 0)"));
    }

    @Test
    public void executeScripts_scrollToBottom() {
        push(KeyCode.N);
        awaitCondition(() -> scriptsExecuted.contains("window.scrollTo(0, document.body.scrollHeight)"));
    }

    @Test
    public void executeScripts_scrollUp() {
        push(KeyCode.J);
        awaitCondition(() -> scriptsExecuted.contains("window.scrollBy(0, -100)"));
    }

    @Test
    public void executeScripts_scrollDown() {
        push(KeyCode.K);
        awaitCondition(() -> scriptsExecuted.contains("window.scrollBy(0, 100)"));
    }

    @Test
    public void jumpToNewCommentBox() {
        // jump to comments
        push(KeyCode.R, 1);
        awaitCondition(() -> hasJumpedToComment);
    }
}
