package util;

public final class GitHubURL {

    public static final String LOGIN_PAGE = "https://github.com/login";

    private static final String RELEASE_BLOB_PREFIX = "https://github.com/HubTurbo/HubTurbo/blob/release/";
    public static final String DOCS_PAGE = RELEASE_BLOB_PREFIX + "docs/userGuide.md";
    public static final String FILTERS_PAGE = RELEASE_BLOB_PREFIX + "docs/filters.md";
    public static final String KEYBOARD_SHORTCUTS_PAGE = RELEASE_BLOB_PREFIX + "docs/keyboardShortcuts.md";

    public static String getPathForAllIssues(String repoId) {
        return String.format("https://github.com/%s/issues", repoId);
    }

    public static String getPathForIssue(String repoId, int id) {
        return String.format("https://github.com/%s/issues/%d", repoId, id);
    }

    public static String getPathForPullRequest(String repoId, int id) {
        return String.format("https://github.com/%s/pull/%d", repoId, id);
    }

    public static String getPathForNewIssue(String repoId) {
        return String.format("https://github.com/%s/issues/new", repoId);
    }

    public static String getPathForNewLabel(String repoId) {
        return String.format("https://github.com/%s/labels", repoId);
    }

    public static String getPathForNewMilestone(String repoId) {
        return String.format("https://github.com/%s/milestones/new", repoId);
    }

    public static String getPathForPullRequests(String repoId) {
        return String.format("https://github.com/%s/pulls", repoId);
    }

    public static String getPathForMilestones(String repoId) {
        return String.format("https://github.com/%s/milestones", repoId);
    }

    public static String getPathForContributors(String repoId) {
        return String.format("https://github.com/%s/graphs/contributors", repoId);
    }

    public static boolean isUrlIssue(String url) {
        return url.matches("https://github.com/([^/]+)/([^/]+)/(issues|pull)/([0-9]+)([/commits,/files]*)");
    }
    
    public static boolean isPullRequestLoaded(String url)  {
        return url.matches("https://github.com/([^/]+)/([^/]+)/pull/([0-9]+)(/|/commits|/files)?");
    }

    /**
     * Checks if current URL is GitHub issue or PR discussion page
     * @param url url to be checked
     */
    public static boolean isUrlIssueOrPrDiscussionPage(String url) {
        return url.matches("https://github.com/([^/]+)/([^/]+)/(issues|pull)/([0-9]+)$");
    }

    private GitHubURL() {}
}
