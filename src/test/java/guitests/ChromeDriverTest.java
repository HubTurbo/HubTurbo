package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import ui.UI;
import util.GitHubURL;
import util.events.IssueCreatedEvent;
import util.events.IssueSelectedEvent;
import util.events.LabelCreatedEvent;
import util.events.MilestoneCreatedEvent;
import util.events.testevents.ExecuteScriptEventHandler;
import util.events.testevents.JumpToCommentEventHandler;
import util.events.testevents.NavigateToPageEventHandler;
import util.events.testevents.SendKeysToBrowserEventHandler;

import static org.junit.Assert.assertEquals;

public class ChromeDriverTest extends UITest {

    private static final int EVENT_DELAY = 1000;
    private String url;
    private String script;
    private String keyCode;

    private boolean jumpToComment = false; // NOPMD

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(
                TestUI.class, "--test=true", "--bypasslogin=true", "--testchromedriver=true");
    }

    @Test
    public void chromeDriverStubTest() {
        clearUrl();
        clearScript();
        clearKeyCode();

        UI.events.registerEvent((NavigateToPageEventHandler) e -> url = e.url);
        UI.events.registerEvent((ExecuteScriptEventHandler) e -> script = e.script);
        UI.events.registerEvent((SendKeysToBrowserEventHandler) e -> keyCode = e.keyCode);
        UI.events.registerEvent((JumpToCommentEventHandler) e -> jumpToComment = true);

        UI.events.triggerEvent(new IssueSelectedEvent("dummy/dummy", 1, 0, false));
        sleep(EVENT_DELAY);
        assertEquals(GitHubURL.getPathForIssue("dummy/dummy", 1), url);
        clearUrl();
        UI.events.triggerEvent(new IssueCreatedEvent());
        sleep(EVENT_DELAY);
        assertEquals(GitHubURL.getPathForNewIssue("dummy/dummy"), url);
        clearUrl();
        UI.events.triggerEvent(new LabelCreatedEvent());
        sleep(EVENT_DELAY);
        assertEquals(GitHubURL.getPathForNewLabel("dummy/dummy"), url);
        clearUrl();
        UI.events.triggerEvent(new MilestoneCreatedEvent());
        sleep(EVENT_DELAY);
        assertEquals(GitHubURL.getPathForNewMilestone("dummy/dummy"), url);
        clearUrl();

        clickIssue(0, 9);
        sleep(EVENT_DELAY);
        clearUrl();

        // show docs
        push(KeyCode.F1);
        sleep(EVENT_DELAY);
        assertEquals(GitHubURL.DOCS_PAGE, url);
        clearUrl();
        push(KeyCode.G).push(KeyCode.H);
        sleep(EVENT_DELAY);
        assertEquals(GitHubURL.DOCS_PAGE, url);
        clearUrl();

        // scroll to top
        push(KeyCode.I);
        sleep(EVENT_DELAY);
        assertEquals("window.scrollTo(0, 0)", script);
        clearScript();

        // scroll to bottom
        push(KeyCode.N);
        sleep(EVENT_DELAY);
        assertEquals("window.scrollTo(0, document.body.scrollHeight)", script);
        clearScript();

        // scroll up
        push(KeyCode.J);
        sleep(EVENT_DELAY);
        assertEquals("window.scrollBy(0, -100)", script);
        clearScript();

        // scroll down
        push(KeyCode.K);
        sleep(EVENT_DELAY);
        assertEquals("window.scrollBy(0, 100)", script);
        clearScript();

        // go to labels page
        push(KeyCode.G).push(KeyCode.L);
        sleep(EVENT_DELAY);
        assertEquals(GitHubURL.getPathForNewLabel("dummy/dummy"), url);
        clearUrl();

        // go to issues page
        push(KeyCode.G).push(KeyCode.I);
        sleep(EVENT_DELAY);
        assertEquals(GitHubURL.getPathForAllIssues("dummy/dummy"), url);
        clearUrl();

        // go to milestones page
        push(KeyCode.G).push(KeyCode.M);
        sleep(EVENT_DELAY);
        assertEquals(GitHubURL.getPathForMilestones("dummy/dummy"), url);
        clearUrl();

        // go to pull requests page
        push(KeyCode.G).push(KeyCode.P);
        sleep(EVENT_DELAY);
        assertEquals(GitHubURL.getPathForPullRequests("dummy/dummy"), url);
        clearUrl();

        // go to developers page
        push(KeyCode.G).push(KeyCode.D);
        sleep(EVENT_DELAY);
        assertEquals(GitHubURL.getPathForContributors("dummy/dummy"), url);
        clearUrl();

        // go to keyboard shortcuts page
        push(KeyCode.G).push(KeyCode.K);
        sleep(EVENT_DELAY);
        assertEquals(GitHubURL.KEYBOARD_SHORTCUTS_PAGE, url);
        clearUrl();

        // manage assignee
        push(KeyCode.A);
        sleep(EVENT_DELAY);
        assertEquals("a", keyCode);
        clearKeyCode();

        // jump to comments
        push(KeyCode.R);
        sleep(EVENT_DELAY);
        assertEquals(true, jumpToComment);
        jumpToComment = false;

        click("View");
        click("Documentation");
        sleep(EVENT_DELAY);
        assertEquals(GitHubURL.DOCS_PAGE, url);
        clearUrl();

        click("File");
        click("Logout");
    }

    public void clearUrl() {
        url = "";
    }

    public void clearScript() {
        script = "";
    }

    public void clearKeyCode() {
        keyCode = "";
    }
}
