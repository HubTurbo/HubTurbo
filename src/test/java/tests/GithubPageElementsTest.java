package tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import util.GithubPageElements;

public class GithubPageElementsTest {
    @Test
    public void gitHubURLPageElementTest() {
        assertEquals(123, (int) GithubPageElements.extractIssueNumber("fixes #123").get());
        assertEquals(123, (int) GithubPageElements.extractIssueNumber("Closed #123. This is dummy").get());
        assertEquals(123, (int) GithubPageElements.extractIssueNumber("refer to #144. Closed #123. This i").get());
    }
}
