package model;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;

import service.ServiceManager;
import storage.CacheFileHandler;
import ui.UI;
import util.CollectionUtilities;
import util.DialogMessage;
import util.events.EventDispatcher;
import util.events.ModelChangedEvent;

/**
 * Aggregates collections of all resources: issues, labels, milestones, users/collaborators.
 * Provides methods to access them, and method for updating them from different sources
 * (cache (Turbo* resource)/GitHub (regular resource)).
 * 
 * When modifying this class, it is important that you modify ModelStub as well, to add
 * stub versions of new methods. Platform.runLater should be removed. Other things like
 * network operations or file access may not be needed depending on the intent of the test.
 * 
 * TODO use a proper data structure in updateCachedList and get rid of untyped methods
 */
@SuppressWarnings("unused")
public class Model {

	private static final Logger logger = LogManager.getLogger(Model.class.getName());
	
	private static final String MESSAGE_LOADING_COLLABS = "Loading collaborators...";
	private static final String MESSAGE_LOADING_LABELS = "Loading labels...";
	private static final String MESSAGE_LOADING_MILESTONES = "Loading milestones...";
	private static final String MESSAGE_LOADING_ISSUES = "Loading issues...";

	protected List<TurboIssue> issues = new ArrayList<>();
	protected ObservableList<TurboUser> collaborators = FXCollections.observableArrayList();
	protected ObservableList<TurboLabel> labels = FXCollections.observableArrayList();
	protected ObservableList<TurboMilestone> milestones = FXCollections.observableArrayList();

	protected IRepositoryIdProvider repoId;

	private String issuesETag = null;
	private String collabsETag = null;
	private String labelsETag = null;
	private String milestonesETag = null;
	private String issueCheckTime = null;

	private CacheFileHandler dcHandler = null;
	
	protected EventDispatcher eventDispatcher = UI.getInstance();

	public Model() {
		setupModelChangeListeners();
	}

	protected void setupModelChangeListeners() {
		collaborators.addListener((ListChangeListener.Change<? extends TurboUser> c) -> {
			eventDispatcher.triggerEvent(new ModelChangedEvent());
		});
		labels.addListener((ListChangeListener.Change<? extends TurboLabel> c) -> {
			eventDispatcher.triggerEvent(new ModelChangedEvent());
		});
		milestones.addListener((ListChangeListener.Change<? extends TurboMilestone> c) -> {
			eventDispatcher.triggerEvent(new ModelChangedEvent());
		});
	}

	private void ______MODEL_FUNCTIONALITY______() {
	}
	
	/**
	 * Notifies subscribers that the model has changed
	 */
	public void triggerModelChangeEvent() {
		eventDispatcher.triggerEvent(new ModelChangedEvent());
	}

	public IRepositoryIdProvider getRepoId() {
		return repoId;
	}

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
	 * @param repoId
	 * @return true on success, false otherwise
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public boolean loadComponents(RepositoryId repoId) throws IOException {
		try {
			HashMap<String, List> items = ServiceManager.getInstance().getResources(repoId);
			populateComponents(repoId, items);
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
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public void forceReloadComponents() throws IOException {
		HashMap<String, List> items = ServiceManager.getInstance().getGitHubResources();
		populateComponents(repoId, items);
	}

	/**
	 * Given a repository id and a data structure containing resources,
	 * populates the fields of this class with them
	 * 
	 * @param repoId
	 * @param resources
	 */
	@SuppressWarnings("rawtypes")
	public void populateComponents(IRepositoryIdProvider repoId, HashMap<String, List> resources) {
		this.repoId = repoId;
		boolean isTurboResource = false;
		boolean isPublicRepo = false;

		// This is made with the assumption that labels of repos will not be
		// empty (even a fresh copy of a repo)
		if (!resources.get(ServiceManager.KEY_LABELS).isEmpty()) {
			if (resources.get(ServiceManager.KEY_LABELS).get(0).getClass() == TurboLabel.class) {
				isTurboResource = true;
			}
			if (resources.get(ServiceManager.KEY_COLLABORATORS).isEmpty()) {
				isPublicRepo = true;
			}
		}

		if (isTurboResource) {
			loadTurboResources(resources);
		} else {
			// is Github Resource
			loadGitHubResources(resources, isPublicRepo);
		}
	}

	/**
	 * Given a data structure containing resources loaded from the cache,
	 * populates the fields of this class with them.
	 * 
	 * @param turboResources
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadTurboResources(HashMap<String, List> turboResources) {
		Platform.runLater(() -> {
			logger.info(MESSAGE_LOADING_COLLABS);
			loadTurboCollaborators((List<TurboUser>) turboResources.get(ServiceManager.KEY_COLLABORATORS));
			logger.info(MESSAGE_LOADING_LABELS);
			loadTurboLabels((List<TurboLabel>) turboResources.get(ServiceManager.KEY_LABELS));
			logger.info(MESSAGE_LOADING_MILESTONES);
			loadTurboMilestones((List<TurboMilestone>) turboResources.get(ServiceManager.KEY_MILESTONES));

			// only get issues now to prevent assertion error in
			// getLabelReference of TurboIssues
			List<TurboIssue> issues = dcHandler.getRepo().getIssues(ServiceManager.getInstance().getModel());
			logger.info(MESSAGE_LOADING_ISSUES);
			loadTurboIssues(issues);
		});
	}

	/**
	 * Given a data structure containing resources loaded from the cache,
	 * populates the fields of this class with them.
	 * 
	 * @param turboResources
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadGitHubResources(HashMap<String, List> resources, boolean isPublicRepo) {
		if (!isPublicRepo) {
			logger.info(MESSAGE_LOADING_COLLABS);
			loadCollaborators((List<User>) resources.get(ServiceManager.KEY_COLLABORATORS));
		} else {
			// Unable to get collaborators for public repo, so there's no point
			// doing the above
			// This is to remove any collaborators from previous repo (from
			// repo-switching)
			clearCollaborators();
		}
		logger.info(MESSAGE_LOADING_LABELS);
		loadLabels((List<Label>) resources.get(ServiceManager.KEY_LABELS));
		logger.info(MESSAGE_LOADING_MILESTONES);
		loadMilestones((List<Milestone>) resources.get(ServiceManager.KEY_MILESTONES));
		logger.info(MESSAGE_LOADING_ISSUES);
		loadIssues((List<Issue>) resources.get(ServiceManager.KEY_ISSUES));
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void updateCachedList(List list, List newList, String repoId) {
		HashMap<String, HashSet> changes = CollectionUtilities.getChangesToList(list, newList);
		HashSet removed = changes.get(CollectionUtilities.REMOVED_TAG);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
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

				dcHandler.writeToFile(repoId, issuesETag, collabsETag, labelsETag, milestonesETag, issueCheckTime,
						collaborators, labels, milestones, issues);
			}

		});
	}

	@SuppressWarnings("rawtypes")
	protected void logNumOfUpdates(List newList, String type) {
		logger.info("Retrieved " + newList.size() + " updated " + type + " since last sync");
	}

	public void refresh() {
		ServiceManager.getInstance().restartModelUpdate();
		UI.getInstance().triggerEvent(new ModelChangedEvent());
	}

	private void ______ISSUES______() {
	}

	public List<TurboIssue> getIssues() {
		return Collections.unmodifiableList(issues);
	}

	/**
	 * Given a list of Issues, loads them into the issue collection.
	 * 
	 * @param ghIssues
	 */
	public void loadIssues(List<Issue> ghIssues) {
		Platform.runLater(() -> {
			issues.clear();
			// Add the issues to a temporary list to prevent a quadratic number
			// of updates to subscribers of the ObservableList
			ArrayList<TurboIssue> buffer = CollectionUtilities.getHubTurboIssueList(ghIssues);
			// Add them all at once, so this propagates only one change
			issues.addAll(buffer);
			triggerModelChangeEvent();
			dcHandler.writeToFile(repoId.toString(), issuesETag, collabsETag, labelsETag, milestonesETag,
					issueCheckTime, collaborators, labels, milestones, issues);
		});
	}

	/**
	 * Given the id of an issue, returns its index in the issue collection TODO
	 * change to optional TODO index may no longer be applicable if we don't use
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
	 * Given the id of a issue, returns a reference to it TODO change to
	 * optional
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

	public void updateCachedIssues(List<Issue> newIssues, String repoId) {

		if (newIssues.size() == 0) {
			assert false : "updateCachedIssues should not be called before issues have been loaded";
			return;
		}

		Platform.runLater(() -> {
			logger.debug(newIssues.size() + " issues changed/added since last sync");
			for (int i = newIssues.size() - 1; i >= 0; i--) {
				Issue issue = newIssues.get(i);
				TurboIssue newCached = new TurboIssue(issue, Model.this);
				updateCachedIssue(newCached);
			}
			triggerModelChangeEvent();
			dcHandler.writeToFile(repoId, issuesETag, collabsETag, labelsETag, milestonesETag, issueCheckTime,
					collaborators, labels, milestones, issues);
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
			tIssue.copyValues(issue);
			logger.debug("Updated issue: " + issue.getId());
		} else {
			issues.add(0, issue);
			logger.info("Added issue: " + issue.getId());
		}
	}

	private void ______LABELS______() {
	}

	public ObservableList<TurboLabel> getLabels() {
		return labels;
	}

	/**
	 * Returns a reference to the TurboLabel given its full name on GitHub.
	 * TODO change to optional
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
		Platform.runLater(() -> {
			labels.add(label);
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
		Platform.runLater(() -> {
			labels.remove(label);
		});
	}

	public void loadLabels(List<Label> ghLabels) {
		Platform.runLater(() -> {
			labels.clear();
			ArrayList<TurboLabel> buffer = CollectionUtilities.getHubTurboLabelList(ghLabels);
			labels.addAll(buffer);
		});
	}

	private void ______CACHED_LABELS______() {
	}

	public void loadTurboLabels(List<TurboLabel> list) {
		labels.clear();
		labels.addAll(list);
	}

	public void updateCachedLabels(List<Label> ghLabels, String repoId) {
		ArrayList<TurboLabel> newLabels = CollectionUtilities.getHubTurboLabelList(ghLabels);
		updateCachedList(labels, newLabels, repoId);
	}

	private void ______MILESTONES______() {
	}

	public ObservableList<TurboMilestone> getMilestones() {
		return milestones;
	}

	public void loadMilestones(List<Milestone> ghMilestones) {
		Platform.runLater(() -> {
			milestones.clear();
			ArrayList<TurboMilestone> buffer = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
			milestones.addAll(buffer);
		});
	}

	/**
	 * Returns a reference to the TurboLabel given its title on GitHub.
	 * TODO change to optional
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
		Platform.runLater(() -> {
			milestones.add(milestone);
		});
	}

	public void deleteMilestone(TurboMilestone milestone) {
		Platform.runLater(() -> {
			milestones.remove(milestone);
		});
	}

	private void ______CACHED_MILESTONES______() {
	}

	public void loadTurboMilestones(List<TurboMilestone> list) {
		milestones.clear();
		milestones.addAll(list);
	}

	public void updateCachedMilestones(List<Milestone> ghMilestones, String repoId) {
		ArrayList<TurboMilestone> newMilestones = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
		updateCachedList(milestones, newMilestones, repoId);
	}

	private void ______COLLABORATORS______() {
	}

	public ObservableList<TurboUser> getCollaborators() {
		return collaborators;
	}

	/**
	 * Returns a reference to a TurboUser given his/her login name on GitHub.
	 * TODO change to optional
	 * TODO make naming of method more consistent, use login for one
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
		Platform.runLater(() -> {
			collaborators.clear();
			collaborators.addAll(CollectionUtilities.getHubTurboUserList(ghCollaborators));
		});
	}

	public void clearCollaborators() {
		Platform.runLater(() -> {
			collaborators.clear();
		});
	}

	private void ______CACHED_COLLABORATORS______() {
	}

	public void loadTurboCollaborators(List<TurboUser> list) {
		collaborators.clear();
		collaborators.addAll(list);
	}

	public void updateCachedCollaborators(List<User> ghCollaborators, String repoId) {
		ArrayList<TurboUser> newCollaborators = CollectionUtilities.getHubTurboUserList(ghCollaborators);
		updateCachedList(collaborators, newCollaborators, repoId);
	}

	private void ______RESOURCE_METADATA______() {
	}

	public void updateIssuesETag(String ETag) {
		this.issuesETag = ETag;
	}

	public void updateCollabsETag(String ETag) {
		this.collabsETag = ETag;
	}

	public void updateLabelsETag(String ETag) {
		this.labelsETag = ETag;
	}

	public void updateMilestonesETag(String ETag) {
		this.milestonesETag = ETag;
	}

	public void updateIssueCheckTime(String date) {
		this.issueCheckTime = date;
	}
}
