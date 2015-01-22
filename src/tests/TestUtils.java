package tests;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

import service.ServiceManager;

public class TestUtils {

	/**
	 * Methods for generating GitHub stub data.
	 */
	
	/**
	 * Generates a data structure containing stub data of the given length, loaded
	 * from a simulated cache. Does not contain issues, since that's how the cache-loaded
	 * data works.
	 * @param model
	 * @param n
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static HashMap<String, List> getStubResources(Model model, int n) {
	    HashMap<String, List> resources = new HashMap<>();
	    resources.put(ServiceManager.KEY_ISSUES, getStubIssues(n));
	    resources.put(ServiceManager.KEY_LABELS, getStubLabels(n));
	    resources.put(ServiceManager.KEY_COLLABORATORS, getStubUsers(n));
	    resources.put(ServiceManager.KEY_MILESTONES, getStubMilestones(n));
	    return resources;
	}

	/**
	 * Generates a list of Issues of the given length.
	 * @param number
	 * @return
	 */
	public static List<Issue> getStubIssues(int number) {
	    ArrayList<Issue> issues = new ArrayList<>();
	    
	    User user = new User();
	    user.setLogin("tester");
	    
	    for (int i=0; i<number; i++) {
	        Issue issue = new Issue();
	        issue.setTitle("issue" + i);
	        issue.setBody("description for issue " + i);
	        issue.setId(i+1);
	        issue.setState("open");
	        issue.setUser(user);
	        issue.setCreatedAt(new Date());
	        issue.setUpdatedAt(new Date());
	        issues.add(issue);
	    }
	    return issues;
	}

	/**
	 * Generates a list of Labels of the given length.
	 * @return
	 */
	public static List<Label> getStubLabels(int number) {
	    ArrayList<Label> labels = new ArrayList<>();
	    for (int i=0; i<number; i++) {
	        Label label = new Label();
	        label.setName("group.label" + (i+1));
	        labels.add(label);
	    }
	    return labels;
	}

	/**
	 * Generates a list of Users of the given length.
	 * @return
	 */
	public static List<User> getStubUsers(int number) {
	    ArrayList<User> users = new ArrayList<>();
	    for (int i=0; i<number; i++) {
	        User user = new User();
	        user.setLogin("user" + (i+1));
	        users.add(user);
	    }
	    return users;
	}

	/**
	 * Generates a list of Milestones of the given length.
	 * @return
	 */
	public static List<Milestone> getStubMilestones(int number) {
	    ArrayList<Milestone> milestones = new ArrayList<>();
	    for (int i=0; i<number; i++) {
	        Milestone milestone = new Milestone();
	        milestone.setTitle("v0."+(i+1));
	        milestones.add(milestone);
	    }
	    return milestones;
	}

	/**
	 * Methods for generating Turbo* stub data.
	 */
	
	/**
	 * Generates a data structure containing stub data of the given length, loaded
	 * from a simulated cache. Does not contain issues, since that's how the cache-loaded
	 * data works.
	 * @param model
	 * @param n
	 * @return
	 */
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
	 * Generates a list of TurboIssues of the given length.
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
	 * Generates a list of TurboLabels of the given length.
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
	 * Generates a list of TurboUsers of the given length.
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
	 * Generates a list of TurboMilestones of the given length.
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

	/**
	 * Factory method for creating TurboLabels in one line.
	 * @param group
	 * @param name
	 * @return
	 */
	private static TurboLabel createTurboLabel(String group, String name) {
		TurboLabel label = new TurboLabel();
		label.setName(name);
		label.setGroup(group);
		return label;
	}

	/**
	 * Factory method for creating TurboUsers in one line.
	 * @param name
	 * @return
	 */
	private static TurboUser createTurboUser(String name) {
		TurboUser user = new TurboUser();
		user.setGithubName(name);
		return user;
	}
}
