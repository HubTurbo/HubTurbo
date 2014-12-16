package util;

import service.ServiceManager;

public class GitHubURL {
	public static final String LOGIN_PAGE = "https://github.com/login";

	public static String getPathForIssue(int id) {
		return String.format("https://github.com/%s/%s/issues/%d", ServiceManager.getInstance().getRepoOwner(), ServiceManager.getInstance().getRepoName(), id);
	}
}
