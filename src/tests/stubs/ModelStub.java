package tests.stubs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import model.Listable;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;

import service.ServiceManager;
import tests.TestUtils;
import util.CollectionUtilities;
import util.events.EventDispatcher;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("unused")
public class ModelStub extends Model {

	public ModelStub() {
		eventDispatcher = new ModelEventDispatcherStub(new EventBus());
	}

	/**
	 * Making the event dispatcher accessible
	 * @param ed
	 */
	public void setEventDispatcher(EventDispatcher ed) {
		eventDispatcher = ed;
	}

	/**
	 * Returns a reference to the actual resource collection, for listening
	 * while testing.
	 * 
	 * @return
	 */
	public List<TurboIssue> getIssuesRef() {
		return issues;
	}

	/**
	 * Returns a reference to the actual resource collection, for listening
	 * while testing.
	 * 
	 * @return
	 */
	public ObservableList<TurboUser> getCollaboratorsRef() {
		return collaborators;
	}

	/**
	 * Returns a reference to the actual resource collection, for listening
	 * while testing.
	 * 
	 * @return
	 */
	public ObservableList<TurboLabel> getLabelsRef() {
		return labels;
	}

	/**
	 * Returns a reference to the actual resource collection, for listening
	 * while testing.
	 * 
	 * @return
	 */
	public ObservableList<TurboMilestone> getMilestonesRef() {
		return milestones;
	}

	private void ______MODEL_FUNCTIONALITY______() {
	}

	/**
	 * Overridden to not perform network access; instead it loads stub data.
	 * This stub data will always be from the cache. Loading from an online
	 * source is tested with forceReloadComponents instead.
	 */
	@Override
	public boolean loadComponents(RepositoryId repoId) {
		this.repoId = repoId;
		populateComponents(repoId, TestUtils.getStubTurboResourcesFromCache(this, 10));
		return true;
	}

	/**
	 * Overridden to not perform network access; instead it loads stub data.
	 * This stub data will always be from an online source. loadComponents tests
	 * the loading of cache data.
	 */
	@Override
	public void forceReloadComponents() {
		populateComponents(repoId, TestUtils.getStubResources(this, 10));
	}

	/**
	 * Overridden to not be wrapped in Platform.runLater.
	 * 
	 * @param turboResources
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void loadTurboResources(HashMap<String, List> turboResources) {

		loadTurboCollaborators((List<TurboUser>) turboResources.get(ServiceManager.KEY_COLLABORATORS));
		loadTurboLabels((List<TurboLabel>) turboResources.get(ServiceManager.KEY_LABELS));
		loadTurboMilestones((List<TurboMilestone>) turboResources.get(ServiceManager.KEY_MILESTONES));

		// Load issues last, and from a separate source
		loadTurboIssues(TestUtils.getStubTurboIssues(this, 10));
	}

	/**
	 * Overridden to remove Platform.runLater, writing to cache
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void updateCachedList(List list, List newList, String repoId) {
		HashMap<String, HashSet> changes = CollectionUtilities.getChangesToList(list, newList);
		HashSet removed = changes.get(CollectionUtilities.REMOVED_TAG);
		list.removeAll(removed);

		Listable listItem = (Listable) newList.get(0);
		if (listItem instanceof TurboMilestone) {
			logNumOfUpdates(newList, "milestone(s)");
		} else if (listItem instanceof TurboLabel) {
			logNumOfUpdates(newList, "label(s)");
		} else if (listItem instanceof TurboUser) {
			logNumOfUpdates(newList, "collaborator(s)");
		}

		ArrayList<Object> buffer = new ArrayList<>();
		for (Object item : newList) {
			int index = list.indexOf(item);
			if (index != -1) {
				Listable old = (Listable) list.get(index);
				old.copyValues(item);
			} else {
				buffer.add(item);
			}
		}
		list.addAll(buffer);
	}

	private void ______ISSUES______() {
	}

	/**
	 * Overridden to not run on the JavaFX Application Thread, and to not write
	 * to cache
	 */
	@Override
	public void loadIssues(List<Issue> ghIssues) {
		issues.clear();
		ArrayList<TurboIssue> buffer = CollectionUtilities.getHubTurboIssueList(ghIssues);
		issues.addAll(buffer);
		triggerModelChangeEvent();
	}

	private void ______CACHED_ISSUES______() {
	}

	/**
	 * Overridden to remove logging, Platform.runLater, and writing to cache
	 */
	@Override
	public void updateCachedIssues(List<Issue> issueList, String repoId) {
		for (int i = issueList.size() - 1; i >= 0; i--) {
			Issue issue = issueList.get(i);
			TurboIssue newCached = new TurboIssue(issue, this);
			updateCachedIssue(newCached);
		}
	}

	private void ______LABELS______() {
	}

	/**
	 * Overridden to remove Platform.runLater
	 */
	@Override
	public void addLabel(TurboLabel label) {
		labels.add(label);
	}

	/**
	 * Overridden to remove Platform.runLater
	 */
	public void loadLabels(List<Label> ghLabels) {
		labels.clear();
		ArrayList<TurboLabel> buffer = CollectionUtilities.getHubTurboLabelList(ghLabels);
		labels.addAll(buffer);
	}

	/**
	 * Overridden to remove Platform.runLater
	 */
	public void deleteLabel(TurboLabel label) {
		labels.remove(label);
	}

	private void ______CACHED_LABELS______() {
	}

	private void ______MILESTONES______() {
	}

	/**
	 * Overridden to remove Platform.runLater
	 */
	@Override
	public void loadMilestones(List<Milestone> ghMilestones) {
		milestones.clear();
		ArrayList<TurboMilestone> buffer = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
		milestones.addAll(buffer);
	}

	/**
	 * Overridden to remove Platform.runLater
	 */
	@Override
	public void addMilestone(TurboMilestone milestone) {
		milestones.add(milestone);
	}

	/**
	 * Overridden to remove Platform.runLater
	 */
	@Override
	public void deleteMilestone(TurboMilestone milestone) {
		milestones.remove(milestone);
	}

	private void ______CACHED_MILESTONES______() {
	}

	private void ______COLLABORATORS______() {
	}

	/**
	 * This is NOT overridden, just used by other tests
	 * 
	 * @param user
	 */
	public void addCollaborator(TurboUser user) {
		collaborators.add(user);
	}

	/**
	 * Overridden to remove Platform.runLater
	 */
	public void loadCollaborators(List<User> ghCollaborators) {
		collaborators.clear();
		collaborators.addAll(CollectionUtilities.getHubTurboUserList(ghCollaborators));
	}

	/**
	 * Overridden to remove Platform.runLater
	 */
	public void clearCollaborators() {
		collaborators.clear();
	}

	private void ______CACHED_COLLABORATORS______() {
	}

	private void ______RESOURCE_METADATA______() {
	}

	private void ______DONE_UNTIL_HERE______() {
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
