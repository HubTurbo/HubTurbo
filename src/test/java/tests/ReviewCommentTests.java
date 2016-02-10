package tests;

import github.ReviewComment;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReviewCommentTests {
    /**
     * Tests ReviewComment class setters/getters
     */
    @Test
    public void testReviewComment() {
        ReviewComment reviewComment = new ReviewComment();

        reviewComment.setCommitId("eb123bfe");
        assertEquals("eb123bfe", reviewComment.getCommitId());

        reviewComment.setDiffHunk("@@ -1,9 +1,10 @@");
        assertEquals("@@ -1,9 +1,10 @@", reviewComment.getDiffHunk());

        reviewComment.setOriginalCommitId("543ab123ef");
        assertEquals("543ab123ef", reviewComment.getOriginalCommitId());

        reviewComment.setOriginalPosition(17);
        assertEquals(17, reviewComment.getOriginalPosition());

        reviewComment.setPath("src/file.txt");
        assertEquals("src/file.txt", reviewComment.getPath());

        reviewComment.setPosition(42);
        assertEquals(42, reviewComment.getPosition());

        reviewComment.setPullRequestUrl("http://api.github.com/repos/owner/repo/pulls/1");
        assertEquals("http://api.github.com/repos/owner/repo/pulls/1", reviewComment.getPullRequestUrl());
    }
}
