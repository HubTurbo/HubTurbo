package tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;
import service.ServiceManager;

public class Test {

	@SuppressWarnings("rawtypes")
	public static HashMap<String, List> getStubTurboResourcesFromCache(Model model, int n) {
		HashMap<String, List> resources = new HashMap<>();
		// Issues are not included here
		resources.put(ServiceManager.KEY_LABELS, getStubTurboLabels(n));
		resources.put(ServiceManager.KEY_COLLABORATORS, getStubTurboUsers(n));
		resources.put(ServiceManager.KEY_MILESTONES, getStubTurboMilestones(n));
		return resources;
	}

	/**
	 * Generates a list of issues of the given length.
	 * @param model
	 * @param number
	 * @return
	 */
	public static List<TurboIssue> getStubTurboIssues(Model model, int number) {
		ArrayList<TurboIssue> issues = new ArrayList<>();
		for (int i=0; i<number; i++) {
			TurboIssue issue = new TurboIssue("issue" + i, "description for issue " + i, model);
			issue.setId(i+1);
			issues.add(issue);
		}
		return issues;
	}

	/**
	 * Generates a list of labels of the given length.
	 * @return
	 */
	public static List<TurboLabel> getStubTurboLabels(int number) {
		ArrayList<TurboLabel> labels = new ArrayList<>();
		for (int i=0; i<number; i++) {
			TurboLabel label = createTurboLabel("group", "name" + (i+1));
			labels.add(label);
		}
		return labels;
	}

	/**
	 * Generates a list of users of the given length.
	 * @return
	 */
	public static List<TurboUser> getStubTurboUsers(int number) {
		ArrayList<TurboUser> users = new ArrayList<>();
		for (int i=0; i<number; i++) {
			TurboUser user = createTurboUser("user" + (i+1));
			users.add(user);
		}
		return users;
	}

	/**
	 * Generates a list of milestones of the given length.
	 * @return
	 */
	public static List<TurboMilestone> getStubTurboMilestones(int number) {
		ArrayList<TurboMilestone> milestones = new ArrayList<>();
		for (int i=0; i<number; i++) {
			TurboMilestone milestone = new TurboMilestone("v0."+(i+1));
			milestones.add(milestone);
		}
		return milestones;
	}

	private static TurboLabel createTurboLabel(String group, String name) {
		TurboLabel label = new TurboLabel();
		label.setName(name);
		label.setGroup(group);
		return label;
	}

	private static TurboUser createTurboUser(String name) {
		TurboUser user = new TurboUser();
		user.setGithubName(name);
		return user;
	}
}
