package tests;

import backend.github.GitHubRepo;
import backend.resource.TurboIssue;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class GitHubRepoTests {
    /**
     * GitHubRepo.getReviewComments should return an empty list if the requested repository is invalid
     */
    @Test
    public void testGetReviewCommentsForInvalidRepo() {
        GitHubRepo repo = new GitHubRepo();
        assertEquals(new ArrayList<>(), repo.getReviewComments("owner/nonexistentrepo", 1));
    }

    /**
     * GitHubRepo.getCommitComments should return an empty list if the requested repository is invalid
     */
    @Test
    public void testGetCommitCommentsForInvalidRepo() {
        GitHubRepo repo = new GitHubRepo();
        assertEquals(new ArrayList<>(), repo.getCommitComments("owner/nonexistentrepo", 1));
    }

    /**
     * GitHubRepo.getAllComments should return an empty list if the requested repository is invalid
     */
    @Test
    public void testGetAllCommentsForInvalidRepo() {
        GitHubRepo repo = new GitHubRepo();
        TurboIssue issue = new TurboIssue("repo", 1, "title", "owner", LocalDateTime.now(), false);
        TurboIssue pullRequest = new TurboIssue("repo", 1, "title", "owner", LocalDateTime.now(), false);

        assertEquals(new ArrayList<>(), repo.getAllComments("owner/nonexistentrepo", issue));
        assertEquals(new ArrayList<>(), repo.getAllComments("owner/nonexistentrepo", pullRequest));
    }
}
