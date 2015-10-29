package tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import util.GithubURLPageElements;

public class GithubURLPageElementsTest {
    @Test
    public void gitHubURLPageElementTest() {
        assertEquals(123, (int) GithubURLPageElements.extractIssueNumber("fixes #123").get());
        assertEquals(123, (int) GithubURLPageElements.extractIssueNumber("Closed #123. This is dummy").get());
        assertEquals(123, (int) GithubURLPageElements.extractIssueNumber("refer to #144. Closed #123. This i").get());
    }
}
