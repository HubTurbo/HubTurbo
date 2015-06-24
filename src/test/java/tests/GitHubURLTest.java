package tests;

import org.junit.Test;
import util.GitHubURL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GitHubURLTest {

    @Test
    public void gitHubURLTest() {
        assertEquals("https://github.com/dummy/dummy/issues", GitHubURL.getPathForAllIssues("dummy/dummy"));
        assertEquals("https://github.com/dummy/dummy/issues/1", GitHubURL.getPathForIssue("dummy/dummy", 1));
        assertEquals("https://github.com/dummy/dummy/pull/2", GitHubURL.getPathForPullRequest("dummy/dummy", 2));
        assertEquals("https://github.com/dummy/dummy/issues/new", GitHubURL.getPathForNewIssue("dummy/dummy"));
        assertEquals("https://github.com/dummy/dummy/labels", GitHubURL.getPathForNewLabel("dummy/dummy"));
        assertEquals("https://github.com/dummy/dummy/milestones/new",
                GitHubURL.getPathForNewMilestone("dummy/dummy"));
        assertEquals("https://github.com/HubTurbo/HubTurbo/wiki/Getting-Started", GitHubURL.getPathForDocsPage());
        assertEquals("https://github.com/dummy/dummy/pulls", GitHubURL.getPathForPullRequests("dummy/dummy"));
        assertEquals("https://github.com/HubTurbo/HubTurbo/wiki/Keyboard-Shortcuts",
                GitHubURL.getPathForKeyboardShortcuts());
        assertEquals("https://github.com/dummy/dummy/milestones", GitHubURL.getPathForMilestones("dummy/dummy"));
        assertEquals("https://github.com/dummy/dummy/graphs/contributors",
                GitHubURL.getPathForContributors("dummy/dummy"));
        assertTrue(GitHubURL.isUrlIssue("https://github.com/dummy/dummy/issues/1"));
    }

}
