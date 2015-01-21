package tests.stubs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

import util.CollectionUtilities;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Listable;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

public class ModelStub extends Model {

	private ObservableList<TurboUser> collaborators = FXCollections.observableArrayList();
	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	private ObservableList<TurboLabel> labels = FXCollections.observableArrayList();
	private ObservableList<TurboMilestone> milestones = FXCollections.observableArrayList();

	public ModelStub() {
		setupCollabStub();
		setupIssueStub();
		setupLabelStub();
		setupMilestoneStub();
	}

	public boolean loadComponents(IRepositoryIdProvider repoId) {
		this.repoId = repoId;
		System.out.println("model " + repoId);
		return true;
	}

	private void setupCollabStub() {
		TurboUser user = new TurboUser();
		user.setGithubName("Test user");
		collaborators.add(user);
	}

	private void setupIssueStub() {
		TurboIssue issue = new TurboIssue("Test", "", this);
		issue.setId(1);
		issues.add(issue);
	}

	private void setupLabelStub() {
		TurboLabel label = new TurboLabel();
		label.setName("label1");
		labels.add(label);
	}

	private void setupMilestoneStub() {
		TurboMilestone ms = new TurboMilestone();
		ms.setTitle("hello");
		milestones.add(ms);
	}

	public ObservableList<TurboIssue> getIssues() {
		return issues;
	}

	public ObservableList<TurboUser> getCollaborators() {
		return collaborators;
	}

	public ObservableList<TurboLabel> getLabels() {
		return labels;
	}

	public ObservableList<TurboMilestone> getMilestones() {
		return milestones;
	}

	@Override
	public void addMilestone(TurboMilestone milestone) {
		// The overridden version of this doesn't run on the JavaFX Application
		// Thread
		milestones.add(milestone);
	}

	@Override
	public void addLabel(TurboLabel label) {
		// The overridedn version of this doesn't run on the JavaFX Application
		// Thread
		labels.add(label);
	}

	public void addCollaborator(TurboUser user) {
		// This isn't overridden
		collaborators.add(user);
	}

	public TurboIssue createIssue(TurboIssue newIssue) {
		appendToCachedIssues(newIssue);
		return newIssue;
	}

	public void updateIssue(TurboIssue originalIssue, TurboIssue editedIssue) {
		this.updateCachedIssue(editedIssue);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateCachedList(List list, List newList) {
		HashMap<String, HashSet> changes = CollectionUtilities.getChangesToList(list, newList);
		HashSet removed = changes.get(CollectionUtilities.REMOVED_TAG);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				list.removeAll(removed);
				newList.stream().forEach(item -> updateCachedListItem((Listable) item, list));
			}
		});

	}

	@SuppressWarnings("unchecked")
	private void updateCachedListItem(Listable updated, @SuppressWarnings("rawtypes") List list) {
		int index = list.indexOf(updated);
		if (index != -1) {
			Listable old = (Listable) list.get(index);
			old.copyValues(updated);
		} else {
			list.add(updated);
		}
	}

	public void updateCachedCollaborators(List<User> ghCollaborators) {
		ArrayList<TurboUser> newCollaborators = CollectionUtilities.getHubTurboUserList(ghCollaborators);
		updateCachedList(collaborators, newCollaborators);
	}

	public void updateCachedLabels(List<Label> ghLabels) {
		ArrayList<TurboLabel> newLabels = CollectionUtilities.getHubTurboLabelList(ghLabels);
		updateCachedList(labels, newLabels);
	}

	public void updateCachedMilestones(List<Milestone> ghMilestones) {
		ArrayList<TurboMilestone> newMilestones = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
		updateCachedList(milestones, newMilestones);
	}

}
