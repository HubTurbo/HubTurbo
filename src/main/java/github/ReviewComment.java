package github;

import org.eclipse.egit.github.core.Comment;

public class ReviewComment extends Comment {

    private static final long serialVersionUID = -5563518235470110428L;

    private String diffHunk;

    private String path;

    private int position;

    private int originalPosition;

    private String commitId;

    private String originalCommitId;

    private String pullRequestUrl;

    public String getDiffHunk() {
        return diffHunk;
    }

    public void setDiffHunk(String diffHunk) {
        this.diffHunk = diffHunk;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getOriginalPosition() {
        return originalPosition;
    }

    public void setOriginalPosition(int originalPosition) {
        this.originalPosition = originalPosition;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getOriginalCommitId() {
        return originalCommitId;
    }

    public void setOriginalCommitId(String originalCommitId) {
        this.originalCommitId = originalCommitId;
    }

    public String getPullRequestUrl() {
        return pullRequestUrl;
    }

    public void setPullRequestUrl(String pullRequestUrl) {
        this.pullRequestUrl = pullRequestUrl;
    }
}
