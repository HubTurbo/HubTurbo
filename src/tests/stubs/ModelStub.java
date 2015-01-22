package tests.stubs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javafx.application.Platform;
import model.Listable;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;

import service.ServiceManager;
import tests.Test;
import util.CollectionUtilities;

public class ModelStub extends Model {

	public ModelStub() {
	}

	/**
	 * Does not set up model change listeners.
	 * TODO needs an alternative way of testing model changes
	 */
	@Override
	protected void setupModelChangeListeners() {
	}
	
	/**
	 * Overridden to not perform network access; instead it loads stub data.
	 * This stub data will always be from the cache. Loading from an online source
	 * is tested with forceReloadComponents instead.
	 */
	@Override
	public boolean loadComponents(RepositoryId repoId) {
		this.repoId = repoId;
		populateComponents(repoId, Test.getStubTurboResourcesFromCache(this, 10));
		return true;
	}

	/**
	 * Overridden to not perform network access; instead it loads stub data.
	 * This stub data will always be from an online source. loadComponents tests the
	 * loading of cache data.
	 */
	@Override
	public void forceReloadComponents() {
		populateComponents(repoId, Test.getStubResources(this, 10));
	}
	
	/**
	 * Overridden to not be wrapped in Platform.runLater.
	 * @param turboResources
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void loadTurboResources(HashMap<String, List> turboResources) {

		loadTurboCollaborators((List<TurboUser>) turboResources.get(ServiceManager.KEY_COLLABORATORS));
		loadTurboLabels((List<TurboLabel>) turboResources.get(ServiceManager.KEY_LABELS));
		loadTurboMilestones((List<TurboMilestone>) turboResources.get(ServiceManager.KEY_MILESTONES));

		// Load issues last, and from a separate source
		loadTurboIssues(Test.getStubTurboIssues(this, 10));

	}
	
	//// DONE UNTIL HERE

	/**
	 * Overridden to not run on the JavaFX Application Thread
	 */
	@Override
	public void addMilestone(TurboMilestone milestone) {
		milestones.add(milestone);
	}

	/**
	 * Overridden to not run on the JavaFX Application Thread
	 */
	@Override
	public void addLabel(TurboLabel label) {
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
