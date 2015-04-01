package model;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javafx.application.Platform;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;

import service.RepositoryResources;
import service.ServiceManager;
import service.UpdateSignature;
import storage.CacheFileHandler;
import test.TestUtils;
import test.stubs.ModelEventDispatcherStub;
import ui.UI;
import ui.components.HTStatusBar;
import util.CollectionUtilities;
import util.DialogMessage;
import util.events.EventDispatcher;
import util.events.ModelChangedEvent;

import com.google.common.eventbus.EventBus;

/**
 * Aggregates collections of all resources: issues, labels, milestones,
 * users/collaborators. Provides methods to access them, and method for updating
 * them from different sources (cache (Turbo* resource)/GitHub (regular
 * resource)).
 *
 * When modifying this class, it is important that you modify ModelStub as well,
 * to add stub versions of new methods. Platform.runLater should be removed.
 * Other things like network operations or file access may not be needed
 * depending on the intent of the test.
 *
 * TODO use a proper data structure in updateCachedList and get rid of untyped
 * methods
 */
@SuppressWarnings("unused")
public class Model {

	private static final Logger logger = LogManager.getLogger(Model.class.getName());

	public static boolean isInTestMode = false;

	private List<TurboIssue> issues = new ArrayList<>();
	private List<TurboUser> collaborators = new ArrayList<>();
	private List<TurboLabel> labels = new ArrayList<>();
	private List<TurboMilestone> milestones = new ArrayList<>();

	// TODO make final when the model is constructed with this
	private IRepositoryIdProvider repoId;

	private CacheFileHandler dcHandler = null;

	private EventDispatcher eventDispatcher = null;
	private EventBus testEvents = null;

	public Model() {
		if (isInTestMode) {
			testEvents = new EventBus();
			eventDispatcher = new ModelEventDispatcherStub(testEvents);
		} else {
			eventDispatcher = UI.getInstance();
		}
	}

	private void ______MODEL_FUNCTIONALITY______() {
	}

	/**
	 * Executes a block of code in the current thread in test mode,
	 * otherwise executes it on the JavaFX thread.
	 * @param action
	 */
	private void run(Runnable action) {
		if (isInTestMode) {
			action.run();
		} else {
			Platform.runLater(action);
		}
	}

	/**
	 * Notifies subscribers that the model has changed. May be disabled to batch
	 * a set of changes.
	 */
	public void triggerModelChangeEvent() {
		if (modelChangeCounter == 0) {
			// TODO in future this should do a deep copy to ensure
			// that the actual model resources are not mutated
			eventDispatcher.triggerEvent(new ModelChangedEvent(getIssues(), getMilestones(), getLabels(), getCollaborators()));
		}
	}

	private int modelChangeCounter = 0;

	public void disableModelChanges() {
		++modelChangeCounter;
	}

	public void enableModelChanges() {
		--modelChangeCounter;
	}

	public IRepositoryIdProvider getRepoId() {
		return repoId;
	}

	// TODO get rid of this
	public void setRepoId(IRepositoryIdProvider repoId) {
		this.repoId = repoId;
	}

	public void setDataCacheFileHandler(CacheFileHandler dcHandler) {
		this.dcHandler = dcHandler;
	}

	/**
	 * Given a repository id, downloads its resources from GitHub, then
	 * populates fields in this class with them.
	 *
	 * Does not perform network operations in test mode, and always loads
	 * stub data from cache.
	 *
	 * @param repoId the repository to load
	 * @param taskUpdate a callback to handle progress updates
	 * @return true on success, false otherwise
	 * @throws IOException
	 */
	public boolean loadComponents(RepositoryId repoId, BiConsumer<String, Float> taskUpdate) throws IOException {
		if (isInTestMode) {
			// TODO will not be needed when the model is constructed with this
			this.repoId = repoId;
			populateComponents(repoId, TestUtils.getStubTurboResourcesFromCache(this, 10));
			return true;
		}

		try {
			RepositoryResources resources = ServiceManager.getInstance().getResources(repoId, taskUpdate);
			populateComponents(repoId, resources);
			return true;
		} catch (SocketTimeoutException e) {
			Platform.runLater(() -> {
				DialogMessage.showWarningDialog("Internet Connection is down",
						"Timeout while loading items from GitHub. Please check your internet connection.");
			});
			logger.info("Timeout while loading items from GitHub: " + e.getLocalizedMessage());
			return false;
		} catch (UnknownHostException e) {
			Platform.runLater(() -> {
				DialogMessage.showWarningDialog("No Internet Connection",
						"Please check your internet connection and try again");
			});
			logger.info("No internet connection: " + e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * Downloads the resources of the current repository from GitHub, bypassing
	 * the cache, then populates the fields of this class with them.
	 *
	 * Does not perform network operations in test mode, instead loading stub
	 * data from cache.
	 *
	 * @param taskUpdate a callback to handle progress updates
	 * @throws IOException
	 */
	public void forceReloadComponents(BiConsumer<String, Float> taskUpdate) throws IOException {
		if (isInTestMode) {
			populateComponents(repoId, TestUtils.getStubResources(this, 10));
			return;
		}

		RepositoryResources items = ServiceManager.getInstance().getGitHubResources(taskUpdate);
		populateComponents(repoId, items);
	}

	/**
	 * Given a repository id and a data structure containing resources,
	 * populates the fields of this class with them
	 *
	 * @param repoId
	 * @param resources
	 */
	public void populateComponents(IRepositoryIdProvider repoId, RepositoryResources resources) {

		this.repoId = repoId;

		boolean loadedFromCache = resources.isCached();
		boolean isPublicRepo = resources.isCached()
			? resources.getTurboUsers().isEmpty()
			: resources.getUsers().isEmpty();

		CountDownLatch latch = new CountDownLatch(4);

		if (loadedFromCache) {
			loadTurboResources(latch, resources);
		} else {
			loadGitHubResources(latch, resources, isPublicRepo);
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error(e.getLocalizedMessage(), e);
		}

		updateCache();
	}

	/**
	 * Given a data structure containing resources loaded from the cache,
	 * populates the fields of this class with them.
	 *
	 * @param turboResources
	 */
	private void loadTurboResources(CountDownLatch latch, RepositoryResources turboResources) {
		run(() -> {
			disableModelChanges();

			logger.info("Loading collaborators from cache...");
			loadTurboCollaborators(turboResources.getTurboUsers());
			latch.countDown();

			logger.info("Loading labels from cache...");
			loadTurboLabels(turboResources.getTurboLabels());
			latch.countDown();

			logger.info("Loading milestones from cache...");
			loadTurboMilestones(turboResources.getTurboMilestones());
			latch.countDown();

			List<TurboIssue> issues = turboResources.getTurboIssues();
			logger.info("Loading issues from cache...");
			loadTurboIssues(issues);
			enableModelChanges();
			triggerModelChangeEvent();
			latch.countDown();
		});
	}

	/**
	 * Given a data structure containing resources loaded from the cache,
	 * populates the fields of this class with them.
	 */
	private void loadGitHubResources(CountDownLatch latch, RepositoryResources resources, boolean isPublicRepo) {
		run(() -> {
			disableModelChanges();
			if (isPublicRepo) {
				// We can't get collaborators from a public repo. Remove any collaborators
				// left over from a previous repo instead.
				logger.info("Public repo: cannot get collaborators");
				clearCollaborators();
			} else {
				logger.info("Loading collaborators from GitHub...");
				loadCollaborators(resources.getUsers());
			}
			latch.countDown();
			logger.info("Loading labels from GitHub...");
			loadLabels(resources.getLabels());
			latch.countDown();
			logger.info("Loading milestones from GitHub...");
			loadMilestones(resources.getMilestones());
			latch.countDown();
			logger.info("Loading issues from GitHub...");
			loadIssues(resources.getIssues());
			enableModelChanges();
			triggerModelChangeEvent();
			latch.countDown();
		});
	}

	/**
	 * Given two lists of one type of resource (one being the current set, and
	 * one being the new set), updates the current set with the contents of the
	 * new set, then writes the current set to cache.
	 *
	 * @param list
	 * @param newList
	 * @param repoId
	 */
	private <T extends TurboResource> void updateCachedList(List<T> list, List<T> newList,
	                                                        String repoId, CompletableFuture<Void> response) {
		if (newList.size() == 0) {
			// No updates to speak of
			return;
		}
		assert !(newList.get(0) instanceof TurboIssue) : "updatedCachedIssues should be used for issues";

		HashMap<String, HashSet<T>> changes = CollectionUtilities.getChangesToList(list, newList);
		HashSet<T> removed = changes.get(CollectionUtilities.REMOVED_TAG);

		run(() -> {
			list.removeAll(removed);

			assert newList.size() > 0;
			String resourceName = newList.get(0).getClass().getSimpleName()
				.replaceAll("Turbo", "").toLowerCase() + "(s)";
			logNumOfUpdates(newList, resourceName);

			ArrayList<T> buffer = new ArrayList<>();
			for (T item : newList) {
				int index = list.indexOf(item);
				if (index != -1) {
					T existingItem = list.get(index);
					existingItem.copyValuesFrom(item);
				} else {
					buffer.add(item);
				}
			}
			list.addAll(buffer);

			response.complete(null);

			if (!isInTestMode) {
				HTStatusBar.addProgress(0.25);
			}
		});
	}

	private <T> void logNumOfUpdates(List<T> newList, String type) {
		if (!isInTestMode) {
			logger.info("Retrieved " + newList.size() + " updated " + type + " since last sync");
		}
	}

	/**
	 * Updates the cache with the provided values for ETags and issueCheckTime
	 */
	public void updateCache(UpdateSignature updateSignature) {
		if (!isInTestMode) {
			dcHandler.writeToFile(repoId.toString(), updateSignature.issuesETag, updateSignature.labelsETag,
				updateSignature.milestonesETag, updateSignature.collaboratorsETag,
				updateSignature.lastCheckTime, collaborators, labels, milestones, issues);
		}
	}

	/**
	 * Updates the cache with default values for ETags (nothing) and issueCheckTime (current time)
	 */
	public void updateCache() {
		if (!isInTestMode) {
			dcHandler.writeToFile(repoId.toString(), null, null, null, null,
				new Date(), collaborators, labels, milestones, issues);
		}
	}

	public void refresh() {
		ServiceManager.getInstance().updateModelNow();
	}

	private void ______ISSUES______() {
	}

	public List<TurboIssue> getIssues() {
		return Collections.unmodifiableList(issues);
	}

	/**
	 * Given a list of Issues, loads them into the issue collection.
	 * @param ghIssues
	 */
	public void loadIssues(List<Issue> ghIssues) {
		issues = CollectionUtilities.getHubTurboIssueList(ghIssues);
		triggerModelChangeEvent();
	}

	/**
	 * Given the id of an issue, returns its index in the issue collection
	 * TODO change to optional
	 * TODO index may no longer be applicable if we don't use
	 * a list to store issues, re-evaluate uses of this method
	 *
	 * @param issueId
	 * @return
	 */
	public int getIndexOfIssue(int issueId) {
		assert issueId >= 1 : "Invalid issue with id " + issueId;
		int i = 0;
		for (TurboIssue issue : issues) {
			if (issue.getId() == issueId) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/**
	 * Given the id of a issue, returns a reference to it
	 * TODO change to optional
	 *
	 * @param issueId
	 * @return
	 */
	public TurboIssue getIssueWithId(int issueId) {
		assert issueId >= 1 : "Invalid issue with id " + issueId;
		for (TurboIssue issue : getIssues()) {
			if (issue.getId() == issueId) {
				return issue;
			}
		}
		return null;
	}

	private void ______CACHED_ISSUES______() {
	}

	public void loadTurboIssues(List<TurboIssue> list) {
		issues.clear();
		issues.addAll(list);
		triggerModelChangeEvent();
	}

	public void appendToCachedIssues(TurboIssue issue) {
		issues.add(0, issue);
		triggerModelChangeEvent();
	}

	public void updateCachedIssues(CompletableFuture<Void> response, List<Issue> newIssues, String repoId) {

		if (newIssues.size() == 0) {
			assert false : "updateCachedIssues should not be called before issues have been loaded";
			return;
		}

		run(() -> {
			logger.info(newIssues.size() + " issues changed/added since last sync");
			for (int i = newIssues.size() - 1; i >= 0; i--) {
				Issue issue = newIssues.get(i);
				TurboIssue newCached = new TurboIssue(issue, Model.this);
				updateCachedIssue(newCached);
			}
			response.complete(null);
		});
	}

	/**
	 * Given a TurboIssue, adds it to the model if it is not yet in it,
	 * otherwise updates the corresponding issue in the model with its fields.
	 *
	 * @param issue
	 */
	public void updateCachedIssue(TurboIssue issue) {
		TurboIssue tIssue = getIssueWithId(issue.getId());
		if (tIssue != null) {
			tIssue.copyValuesFrom(issue);
			logger.info("Updated issue: " + issue.getId());
		} else {
			issues.add(0, issue);
			logger.info("Added issue: " + issue.getId());
		}
	}

	private void ______LABELS______() {
	}

	public List<TurboLabel> getLabels() {
		return Collections.unmodifiableList(labels);
	}

	/**
	 * Returns a reference to the TurboLabel given its full name on GitHub.
	 * TODO change to optional
	 *
	 * @param name
	 * @return
	 */
	public TurboLabel getLabelByGhName(String name) {
		for (int i = 0; i < labels.size(); i++) {
			if (labels.get(i).toGhName().equals(name)) {
				return labels.get(i);
			}
		}
		return null;
	}

	public void addLabel(TurboLabel label) {
		run(() -> {
			labels.add(label);
			triggerModelChangeEvent();
		});
	}

	/**
	 * Tests to see if all labels of a group are exclusive. If one is not
	 * exclusive, all are not exclusive.
	 *
	 * @param group
	 * @return
	 */
	public boolean isExclusiveLabelGroup(String group) {
		List<TurboLabel> labelsInGrp = labels.stream().filter(l -> group.equals(l.getGroup()))
				.collect(Collectors.toList());

		for (TurboLabel label : labelsInGrp) {
			if (!label.isExclusive()) {
				return false;
			}
		}
		return true;
	}

	public void deleteLabel(TurboLabel label) {
		run(() -> {
			labels.remove(label);
			triggerModelChangeEvent();
		});
	}

	public void loadLabels(List<Label> ghLabels) {
		labels = CollectionUtilities.getHubTurboLabelList(ghLabels);
		triggerModelChangeEvent();
	}

	private void ______CACHED_LABELS______() {
	}

	public void loadTurboLabels(List<TurboLabel> list) {
		labels = new ArrayList<>(list);
		triggerModelChangeEvent();
	}

	public void updateCachedLabels(CompletableFuture<Void> response, List<Label> ghLabels, String repoId) {
		ArrayList<TurboLabel> newLabels = CollectionUtilities.getHubTurboLabelList(ghLabels);
		updateCachedList(labels, newLabels, repoId, response);
	}

	private void ______MILESTONES______() {
	}

	public List<TurboMilestone> getMilestones() {
		return Collections.unmodifiableList(milestones);
	}

	public void loadMilestones(List<Milestone> ghMilestones) {
		milestones = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
		triggerModelChangeEvent();
	}

	/**
	 * Returns a reference to the TurboLabel given its title on GitHub.
	 * TODO change to optional
	 *
	 * @param title
	 * @return
	 */
	public TurboMilestone getMilestoneByTitle(String title) {
		for (int i = 0; i < milestones.size(); i++) {
			if (milestones.get(i).getTitle().equals(title)) {
				return milestones.get(i);
			}
		}
		return null;
	}

	public void addMilestone(TurboMilestone milestone) {
		run(() -> {
			milestones.add(milestone);
			triggerModelChangeEvent();
		});
	}

	public void deleteMilestone(TurboMilestone milestone) {
		run(() -> {
			milestones.remove(milestone);
			triggerModelChangeEvent();
		});
	}

	private void ______CACHED_MILESTONES______() {
	}

	public void loadTurboMilestones(List<TurboMilestone> list) {
		milestones.clear();
		milestones.addAll(list);
		triggerModelChangeEvent();
	}

	public void updateCachedMilestones(CompletableFuture<Void> response,
	                                   List<Milestone> ghMilestones, String repoId) {
		ArrayList<TurboMilestone> newMilestones = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
		updateCachedList(milestones, newMilestones, repoId, response);
	}

	private void ______COLLABORATORS______() {
	}

	public void addCollaborator(TurboUser user) {
		collaborators.add(user);
		triggerModelChangeEvent();
	}

	public List<TurboUser> getCollaborators() {
		return Collections.unmodifiableList(collaborators);
	}

	/**
	 * Returns a reference to a TurboUser given his/her login name on GitHub.
	 * TODO change to optional TODO make naming of method more consistent, use
	 * login for one
	 *
	 * @param name
	 * @return
	 */
	public TurboUser getUserByGhName(String name) {
		for (int i = 0; i < collaborators.size(); i++) {
			if (collaborators.get(i).getGithubName().equals(name)) {
				return collaborators.get(i);
			}
		}
		return null;
	}

	public void loadCollaborators(List<User> ghCollaborators) {
		collaborators = CollectionUtilities.getHubTurboUserList(ghCollaborators);
		triggerModelChangeEvent();
	}

	public void clearCollaborators() {
		collaborators.clear();
		triggerModelChangeEvent();
	}

	private void ______CACHED_COLLABORATORS______() {
	}

	public void loadTurboCollaborators(List<TurboUser> list) {
		collaborators.clear();
		collaborators.addAll(list);
		triggerModelChangeEvent();
	}

	public void updateCachedCollaborators(CompletableFuture<Void> response,
	                                      List<User> ghCollaborators, String repoId) {
		ArrayList<TurboUser> newCollaborators = CollectionUtilities.getHubTurboUserList(ghCollaborators);
		updateCachedList(collaborators, newCollaborators, repoId, response);
	}

	private void ______RESOURCE_METADATA______() {
	}

	private void ______TESTING______() {
	}

	public EventBus getTestEvents() {
		assert isInTestMode : "This function should not be called outside test mode";
		return testEvents;
	}

	public List<TurboIssue> getIssuesRef() {
		assert isInTestMode : "This function should not be called outside test mode";
		return issues;
	}

	public List<TurboUser> getCollaboratorsRef() {
		assert isInTestMode : "This function should not be called outside test mode";
		return collaborators;
	}

	public List<TurboLabel> getLabelsRef() {
		assert isInTestMode : "This function should not be called outside test mode";
		return labels;
	}

	public List<TurboMilestone> getMilestonesRef() {
		assert isInTestMode : "This function should not be called outside test mode";
		return milestones;
	}
}
