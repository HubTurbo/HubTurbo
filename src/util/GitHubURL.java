package util;

import service.ServiceManager;

public class GitHubURL {
	public static final String LOGIN_PAGE = "https://github.com/login";

	public static String getPathForIssue(int id) {
		return String.format("https://github.com/%s/%s/issues/%d", ServiceManager.getInstance().getRepoOwner(), ServiceManager.getInstance().getRepoName(), id);
	}
	
	public static String getPathForNewIssue() {
		return String.format("https://github.com/%s/%s/issues/new", ServiceManager.getInstance().getRepoOwner(), ServiceManager.getInstance().getRepoName());
	}

	public static String getPathForNewLabel() {
		return String.format("https://github.com/%s/%s/labels", ServiceManager.getInstance().getRepoOwner(), ServiceManager.getInstance().getRepoName());
	}

	public static String getPathForNewMilestone() {
		return String.format("https://github.com/%s/%s/milestones/new", ServiceManager.getInstance().getRepoOwner(), ServiceManager.getInstance().getRepoName());
	}
}
