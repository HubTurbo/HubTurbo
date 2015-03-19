package util;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import service.ServiceManager;

public class GitHubURL {
	private static final Logger logger = LogManager.getLogger(GitHubURL.class.getName());
	
	public static final String LOGIN_PAGE = "https://github.com/login";
	public static final String DOCS_PAGE = "https://github.com/HubTurbo/HubTurbo/wiki/Getting-Started";
	public static final String CHANGELOG_PAGE = "https://github.com/HubTurbo/HubTurbo/wiki/Changelog";
	public static final String CHANGELOG_PAGE_FORMAT = "https://github.com/HubTurbo/HubTurbo/wiki/Changelog#v%d%d%d";
	
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

	public static String getPathForDocsPage() {
		return DOCS_PAGE;
	}

	public static String getChangelogForVersion(String version) {
		Optional<int[]> numbers = Utility.parseVersionNumber(version);
		if (numbers.isPresent()) {
			int major = numbers.get()[0];
			int minor = numbers.get()[1];
			int patch = numbers.get()[2];
			return String.format(CHANGELOG_PAGE_FORMAT, major, minor, patch);
		} else {
			logger.error("Invalid version string format " + version + "; going to generic changelog page");
			return CHANGELOG_PAGE;
		}
	}
	
}
