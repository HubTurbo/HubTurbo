package guitests;

import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxToolkit;

import ui.UI;
import util.GitHubURL;
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
    public void cleanAndRegisterHandlers() {
        urlsNavigated.clear();
        scriptsExecuted.clear();
        keysSentToBrowser.clear();
        hasJumpedToComment = false;
        UI.events.registerEvent(navToPageHandler);
        UI.events.registerEvent(execScriptHandler);
        UI.events.registerEvent(jumpCommentHandler);
        clickIssue(0, 9);
    }

    @After
    public void unregisterHandlers() {
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
        responses.clear();
    }

    @Test
    public void navigateToPage() {
        actAndAwaitResponse(
            GitHubURL.getPathForIssue("dummy/dummy", 1),
            () -> UI.events.triggerEvent(new IssueSelectedEvent("dummy/dummy", 1, 0, false)),
            urlsNavigated
        );
        actAndAwaitResponse(
            GitHubURL.getPathForNewIssue("dummy/dummy"),
            () -> UI.events.triggerEvent(new IssueCreatedEvent()),
            urlsNavigated
        );
        actAndAwaitResponse(
            GitHubURL.getPathForNewLabel("dummy/dummy"),
            () -> UI.events.triggerEvent(new LabelCreatedEvent()),
            urlsNavigated
        );
        actAndAwaitResponse(
            GitHubURL.getPathForNewMilestone("dummy/dummy"),
            () -> UI.events.triggerEvent(new MilestoneCreatedEvent()),
            urlsNavigated
        );

        // show docs
        actAndAwaitResponse(
            GitHubURL.DOCS_PAGE,
            () -> push(KeyCode.F1),
            urlsNavigated
        );
        actAndAwaitResponse(
            GitHubURL.DOCS_PAGE,
            () -> {
                push(KeyCode.G);
                push(KeyCode.H);
            },
            urlsNavigated
        );
        actAndAwaitResponse(
            GitHubURL.DOCS_PAGE,
            () -> {
                clickOn("View");
                clickOn("Documentation");
            },
            urlsNavigated
        );
        // go to labels page
        actAndAwaitResponse(
            GitHubURL.getPathForNewLabel("dummy/dummy"),
            () -> push(KeyCode.G).push(KeyCode.L),
            urlsNavigated
        );

        // go to issues page
        actAndAwaitResponse(
            GitHubURL.getPathForAllIssues("dummy/dummy"),
            () -> push(KeyCode.G).push(KeyCode.I),
            urlsNavigated
        );

        // go to milestones page
        actAndAwaitResponse(
            GitHubURL.getPathForMilestones("dummy/dummy"),
            () -> push(KeyCode.G).push(KeyCode.M),
            urlsNavigated
        );

        // go to pull requests page
        actAndAwaitResponse(
            GitHubURL.getPathForPullRequests("dummy/dummy"),
            () -> push(KeyCode.G).push(KeyCode.P),
            urlsNavigated
        );

        // go to developers page
        actAndAwaitResponse(
            GitHubURL.getPathForContributors("dummy/dummy"),
            () -> push(KeyCode.G).push(KeyCode.D),
            urlsNavigated
        );

        // go to keyboard shortcuts page
        actAndAwaitResponse(
            GitHubURL.KEYBOARD_SHORTCUTS_PAGE,
            () -> push(KeyCode.G).push(KeyCode.K),
            urlsNavigated
        );
    }

    @Test
    public void executeScripts() {
        // scroll to top
        actAndAwaitResponse(
            "window.scrollTo(0, 0)",
            () -> push(KeyCode.I),
            scriptsExecuted)
        ;

        // scroll to bottom
        actAndAwaitResponse(
            "window.scrollTo(0, document.body.scrollHeight)",
            () -> push(KeyCode.N),
            scriptsExecuted
        );

        // scroll up
        actAndAwaitResponse(
            "window.scrollBy(0, -100)",
            () -> push(KeyCode.J),
            scriptsExecuted
        );

        // scroll down
        actAndAwaitResponse(
            "window.scrollBy(0, 100)",
            () -> push(KeyCode.K),
            scriptsExecuted
        );
    }

    @Test
    public void jumpToNewCommentBox() {
        // jump to comments
        push(KeyCode.R, 1);
        awaitCondition(() -> hasJumpedToComment);
    }

}
