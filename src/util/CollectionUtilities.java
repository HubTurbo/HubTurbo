package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

import service.ServiceManager;

public class CollectionUtilities {
	public static final String REMOVED_TAG = "removed";
	public static final String ADDED_TAG = "added";

	/**
	 * Gets the changes made to the a list of items
	 * 
	 * @return HashMap the a list of items removed from the original list and a
	 *         list of items added to the original list
	 * */
	public static <T> HashMap<String, HashSet<T>> getChangesToList(
			List<T> original, List<T> edited) {
		HashMap<String, HashSet<T>> changeSet = new HashMap<String, HashSet<T>>();
		HashSet<T> removed = new HashSet<T>(original);
		HashSet<T> added = new HashSet<T>(edited);
		removed.removeAll(edited);
		added.removeAll(original);

		changeSet.put(REMOVED_TAG, removed);
		changeSet.put(ADDED_TAG, added);

		return changeSet;
	}

	public static ArrayList<TurboIssue> getHubTurboIssueList(List<Issue> issues) {
		ArrayList<TurboIssue> buffer = new ArrayList<>();
		for (Issue ghIssue : issues) {
			buffer.add(new TurboIssue(ghIssue, ServiceManager.getInstance()
					.getModel()));
		}
		return buffer;
	}

	public static ArrayList<TurboLabel> getHubTurboLabelList(List<Label> labels) {
		ArrayList<TurboLabel> buffer = new ArrayList<>();
		for (Label ghLabel : labels) {
			buffer.add(new TurboLabel(ghLabel));
		}
		return buffer;
	}

	public static ArrayList<TurboMilestone> getHubTurboMilestoneList(
			List<Milestone> milestones) {
		ArrayList<TurboMilestone> buffer = new ArrayList<>();
		for (Milestone ghMilestone : milestones) {
			buffer.add(new TurboMilestone(ghMilestone));
		}
		return buffer;
	}

	public static ArrayList<TurboUser> getHubTurboUserList(List<User> users){
		ArrayList<TurboUser> buffer = new ArrayList<>();
		for (User ghUser : users) {
			buffer.add(new TurboUser(ghUser));
		}
		return buffer;
	}

	public static ArrayList<Label> getGithubLabelList(List<TurboLabel> labels) {
		ArrayList<Label> githubLabels = new ArrayList<Label>();
		for (TurboLabel label : labels) {
			githubLabels.add(label.toGhResource());
		}
		return githubLabels;
	}
}