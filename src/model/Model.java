package model;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javafx.application.Platform;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;

import service.ServiceManager;
import storage.DataCacheFileHandler;
import util.CollectionUtilities;
import util.DialogMessage;


public class Model {
	private static final Logger logger = LogManager.getLogger(Model.class.getName());
	public static final String STATE_ALL = "all";
	public static final String STATE_OPEN = "open";
	public static final String STATE_CLOSED = "closed";
	private static final String MESSAGE_LOADING_COLLABS = "Loading collaborators...";
	private static final String MESSAGE_LOADING_LABELS = "Loading labels...";
	private static final String MESSAGE_LOADING_MILESTONES = "Loading milestones...";
	private static final String MESSAGE_LOADING_ISSUES = "Loading issues...";
	
	private ConcurrentHashMap<Integer, List<Comment>> cachedGithubComments = new ConcurrentHashMap<Integer, List<Comment>>();
	
	private ArrayList<Runnable> methodsOnChange = new ArrayList<Runnable>();
	
	protected IRepositoryIdProvider repoId;
	
	private String issuesETag = null;
	private String collabsETag = null;
	private String labelsETag = null;
	private String milestonesETag = null;
	private String issueCheckTime = null;
	private DataCacheFileHandler dcHandler = null;
			
	public Model(){
	}

	/**
	 * Issues
	 * 
	 * The model maintains a list of issues. This list is strictly data-only.
	 * It should not be observed or tied to the GUI in any way.
	 * It should not be accessed or changed, even within this class, by any methods
	 * other than the following.
	 * It may only be accessed in a read-only manner if the following methods aren't
	 * used.
	 */
	
	private ArrayList<TurboIssue> _issues = new ArrayList<>();

	private void addIssueToStart(TurboIssue issue) {
		_issues.add(0, issue);
		applyChangeMethods();
	}

	private void addIssueToEnd(TurboIssue issue) {
		_issues.add(issue);
		applyChangeMethods();
	}

	private void changeIssues(List<TurboIssue> newIssues) {
		_issues = new ArrayList<>(newIssues);
		applyChangeMethods();
	}

	public List<TurboIssue> getIssues() {
		return Collections.unmodifiableList(_issues);
	}
	
	/**
	 * Collaborators
	 */
	
	private List<TurboUser> _collaborators = new ArrayList<>();

	private void changeCollaborators(List<TurboUser> newCollaborators) {
		_collaborators = new ArrayList<>(newCollaborators);
		applyChangeMethods();
	}
	
	private void removeAllCollaborators() {
		// TODO remove clearCollaborators once it's no longer used
		_collaborators = new ArrayList<>();
		applyChangeMethods();
	}

	public List<TurboUser> getCollaborators() {
		return Collections.unmodifiableList(_collaborators);
	}

	/**
	 * Labels
	 */

	private List<TurboLabel> _labels = new ArrayList<>();

	private void changeLabels(List<TurboLabel> newLabels) {
		_labels = new ArrayList<>(newLabels);
		applyChangeMethods();
	}

	private void addLabelToEnd(TurboLabel label) {
		// TODO remove addLabel once it's unused
		_labels.add(label);
		applyChangeMethods();
	}

	private void removeLabel(TurboLabel label){
		// TODO remove deleteLabel once it's unused
		_labels.remove(label);
		applyChangeMethods();
	}

	public List<TurboLabel> getLabels() {
		return Collections.unmodifiableList(_labels);
	}

	/**
	 * Milestones
	 */
	
	private List<TurboMilestone> _milestones = new ArrayList<>();

	private void changeMilestones(List<TurboMilestone> newMilestones) {
		_milestones = new ArrayList<>(newMilestones);
		applyChangeMethods();
	}

	private void addMilestoneToEnd(TurboMilestone milestone) {
		// TODO remove addMilestone once it's unused
		_milestones.add(milestone);
		applyChangeMethods();
	}

	private void removeMilestone(TurboMilestone milestone){
		// TODO remove deleteMilestone once it's unused
		_milestones.remove(milestone);
		applyChangeMethods();
	}

	public List<TurboMilestone> getMilestones() {
		return Collections.unmodifiableList(_milestones);
	}
	
	/**
	 * Others
	 */

	public IRepositoryIdProvider getRepoId(){
		return repoId;
	}
	
	public void setRepoId(IRepositoryIdProvider repoId) {
		this.repoId = repoId;
	}
	
	public void setDataCacheFileHandler(DataCacheFileHandler dcHandler) {
		this.dcHandler  = dcHandler;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean loadComponents(RepositoryId repoId) throws IOException{
		try{
			HashMap<String, List> items =  ServiceManager.getInstance().getResources(repoId);
			loadComponents(repoId, items);
			return true;
		} catch(SocketTimeoutException e){
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("Internet Connection is down", 
						"Timeout while loading items from GitHub. Please check your internet connection.");
			});
			logger.info("Timeout while loading items from GitHub: " + e.getLocalizedMessage());
			return false;
		} catch(UnknownHostException e){
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("No Internet Connection", 
						"Please check your internet connection and try again");
			});
			logger.info("No internet connection: " + e.getLocalizedMessage());
			return false;
		}
	}
	
	public void forceReloadComponents() throws IOException{
		HashMap<String, List> items =  ServiceManager.getInstance().getGitHubResources();
		loadComponents(repoId, items);
	}

	/**
	 * Given the resources for a repository, populates the application's state with them.
	 * @param repoId the repository these resources belong to
	 * @param resources a data structure containing the resources for a repository
	 */
	@SuppressWarnings("rawtypes")
	public void loadComponents(IRepositoryIdProvider repoId, HashMap<String, List> resources) {
		this.repoId = repoId;
		cachedGithubComments = new ConcurrentHashMap<Integer, List<Comment>>();
		boolean isTurboResource = false;
		boolean isPublicRepo = false;
		
		// This is made with the assumption that labels of repos will not be empty (even a fresh copy of a repo)
		if (!resources.get(ServiceManager.KEY_LABELS).isEmpty()) {
			if (resources.get(ServiceManager.KEY_LABELS).get(0) instanceof TurboLabel) {
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadTurboResources(HashMap<String, List> turboResources) {
		logger.info(MESSAGE_LOADING_COLLABS);
		loadTurboCollaborators((List<TurboUser>) turboResources.get(ServiceManager.KEY_COLLABORATORS));
		logger.info(MESSAGE_LOADING_LABELS);
		loadTurboLabels((List<TurboLabel>) turboResources.get(ServiceManager.KEY_LABELS));
		logger.info(MESSAGE_LOADING_MILESTONES);
		loadTurboMilestones((List<TurboMilestone>) turboResources.get(ServiceManager.KEY_MILESTONES));

		// only get issues now to prevent assertion error in getLabelReference of TurboIssues
		List<TurboIssue> issues = dcHandler.getRepo().getIssues(ServiceManager.getInstance().getModel());
		logger.info(MESSAGE_LOADING_ISSUES);
		loadTurboIssues(issues);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadGitHubResources(HashMap<String, List> resources, boolean isPublicRepo) {
		if (!isPublicRepo) {
			logger.info(MESSAGE_LOADING_COLLABS);
			loadCollaborators((List<User>) resources.get(ServiceManager.KEY_COLLABORATORS));
		} else {
			// Unable to get collaborators for public repo, so there's no point doing the above
			// This is to remove any collaborators from previous repo (from repo-switching)
			clearCollaborators();
		}
		logger.info(MESSAGE_LOADING_LABELS);
		loadLabels((List<Label>) resources.get(ServiceManager.KEY_LABELS));
		logger.info(MESSAGE_LOADING_MILESTONES);
		loadMilestones((List<Milestone>) resources.get(ServiceManager.KEY_MILESTONES));
		logger.info(MESSAGE_LOADING_ISSUES);
		loadIssues((List<Issue>)resources.get(ServiceManager.KEY_ISSUES));
	}

	public void applyMethodOnModelChange(Runnable method){
		methodsOnChange.add(method);
	}	
	
	public void applyChangeMethods(){
		for(Runnable method : methodsOnChange){
			method.run();
		}
	}
	
	public void cacheCommentsListForIssue(List<Comment> comments, int issueId){
		cachedGithubComments.put(issueId, new ArrayList<Comment>(comments));
	}
	
	public List<Comment>getCommentsListForIssue(int issueId){
		return cachedGithubComments.get(issueId);
	}
 		
	public void appendToCachedIssues(TurboIssue issue){
		addIssueToStart(issue);
	}
	
	public boolean isExclusiveLabelGroup(String group){
		List<TurboLabel> labelsInGrp = getLabels().stream()
			.filter(l -> group.equals(l.getGroup()))
			.collect(Collectors.toList());
		
		for(TurboLabel label : labelsInGrp){
			if(!label.isExclusive()){
				return false;
			}
		}
		return true;
	}

	public void updateCachedIssues(List<Issue> issueList, String repoId){
		if (issueList.size() == 0){
			// should not happen
			return;
		} else {
			//enforceStatusStateConsistency(issueList);
		}
		WeakReference<Model> selfRef = new WeakReference<Model>(this);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {	
				logger.debug(issueList.size() + " issues changed/added since last sync");
				for (int i = issueList.size() - 1; i >= 0; i--) {
					Issue issue = issueList.get(i);
					TurboIssue newCached = new TurboIssue(issue, selfRef.get());
					updateCachedIssue(newCached);
				}
				dcHandler.writeToFile(repoId, issuesETag, collabsETag, labelsETag, milestonesETag, issueCheckTime, getCollaborators(), getLabels(), getMilestones(), getIssues());
			}
		});
	}
		
	public void updateCachedIssue(TurboIssue issue){
		TurboIssue tIssue = getIssueWithId(issue.getId());
		if(tIssue != null){
			tIssue.copyValues(issue);
			logger.debug("Updated issue: " + issue.getId());
		}else{		
			addIssueToStart(tIssue);
			logger.info("Added issue: " + issue.getId());
		}
	}
	
	public void addLabel(TurboLabel label){
		Platform.runLater(()->{
			addLabelToEnd(label);
		});
	}
	
	public void deleteLabel(TurboLabel label){
		Platform.runLater(()->{
			removeLabel(label);
		});
	}
	
	public void addMilestone(TurboMilestone milestone){
		Platform.runLater(()->{
			addMilestoneToEnd(milestone);
		});
	}
	
	public void deleteMilestone(TurboMilestone milestone){
		Platform.runLater(()->{
			removeMilestone(milestone);
		});
	}

	public int getIndexOfIssue(int id){
		List<TurboIssue> issues = getIssues();
		for(int i = 0; i < issues.size(); i++){
			if(((TurboIssue)(issues.get(i))).getId() == id){
				return i;
			}
		}
		return -1;
	}
	
	public TurboIssue getIssueWithId(int id){
		if(id <= 0){
			return null;
		}

		List<TurboIssue> issues = getIssues();
		
		for(int i = 0; i < issues.size(); i++){
			TurboIssue issue = issues.get(i);
			if(issue.getId() == id){
				return issue;
			}
		}
		
		return null;
	}
	
	public TurboLabel getLabelByGhName(String name) {
		List<TurboLabel> labels = getLabels();
		for (int i=0; i<labels.size(); i++) {
			if (labels.get(i).toGhName().equals(name)) {
				return labels.get(i);
			}
		}
		return null;
	}
	
	public TurboMilestone getMilestoneByTitle(String title) {
		List<TurboMilestone> milestones = getMilestones();
		for (int i=0; i<milestones.size(); i++) {
			if (milestones.get(i).getTitle().equals(title)) {
				return milestones.get(i);
			}
		}
		return null;
	}
	
	public TurboUser getUserByGhName(String name) {
		List<TurboLabel> labels = getLabels();
		List<TurboUser> collaborators = getCollaborators();
		for (int i=0; i<labels.size(); i++) {
			if (collaborators.get(i).getGithubName().equals(name)) {
				return collaborators.get(i);
			}
		}
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateCachedList(List list, List newList, String repoId){
		HashMap<String, HashSet> changes = CollectionUtilities.getChangesToList(list, newList);
		HashSet removed = changes.get(CollectionUtilities.REMOVED_TAG);
		Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	  	
				ArrayList<Object> additions = new ArrayList<>();
				for (Object item : newList) {
					int index = list.indexOf(item);
					if (index != -1) {
						Listable old = (Listable) list.get(index);
						old.copyValues(item);
					} else {
						additions.add(item);
					}
				}
	        	
	        	List finalList = new ArrayList<>(list);
	        	finalList.removeAll(removed);
	        	finalList.addAll(additions);
	        	
	        	Listable listItem = (Listable)newList.get(0);
	        	if (listItem instanceof TurboMilestone) {
	        		logNumOfUpdates(newList, "milestone");
	        		changeMilestones(finalList);
	        	} else if (listItem instanceof TurboLabel) {
	        		logNumOfUpdates(newList, "label");
	        		changeLabels(finalList);
	        	} else if (listItem instanceof TurboUser) {
	        		logNumOfUpdates(newList, "collaborator");
	        		changeCollaborators(finalList);
	        	} else {
	        		// TODO remove this once ad-hoc polymorphism removed
	        		assert false : "updateCachedList called with invalid type " + listItem.getClass().getName();
	        	}
	        	
	        	dcHandler.writeToFile(repoId, issuesETag, collabsETag, labelsETag, milestonesETag, issueCheckTime, getCollaborators(), getLabels(), getMilestones(), getIssues());
	        }

			private void logNumOfUpdates(List newList, String type) {
				logger.info("Retrieved " + newList.size() + " updated " + type + "(s) since last sync");
			}
	   });
	}
	
	public void loadCollaborators(List<User> ghCollaborators) {
		
		
		Platform.runLater(()->{
			changeCollaborators(CollectionUtilities.getHubTurboUserList(ghCollaborators));
		});
	}
	
	public void clearCollaborators() {	
		Platform.runLater(()->{
			removeAllCollaborators();
		});
	}
	
	public void loadTurboCollaborators(List<TurboUser> list) {
		changeCollaborators(list);
	}
	
	public void updateCachedCollaborators(List<User> ghCollaborators, String repoId){
		ArrayList<TurboUser> newCollaborators = CollectionUtilities.getHubTurboUserList(ghCollaborators);
		updateCachedList(getCollaborators(), newCollaborators, repoId);
	}
		
	public void loadIssues(List<Issue> ghIssues) {
		if (ghIssues != null) {
			//enforceStatusStateConsistency(ghIssues);
		}
		Platform.runLater(()->{
			changeIssues(CollectionUtilities.getHubTurboIssueList(ghIssues));
			dcHandler.writeToFile(repoId.toString(), issuesETag, collabsETag, labelsETag, milestonesETag, issueCheckTime, getCollaborators(), getLabels(), getMilestones(), getIssues());
		});
	}
	
	public void loadTurboIssues(List<TurboIssue> list) {
		changeIssues(list);
	}

	public void loadLabels(List<Label> ghLabels){
		Platform.runLater(()->{
			changeLabels(CollectionUtilities.getHubTurboLabelList(ghLabels));
		});
	}
	
	public void loadTurboLabels(List<TurboLabel> list) {
		changeLabels(list);
	}
	
	public void updateCachedLabels(List<Label> ghLabels, String repoId){
		ArrayList<TurboLabel> newLabels = CollectionUtilities.getHubTurboLabelList(ghLabels);
		updateCachedList(getLabels(), newLabels, repoId);	
	}
	
	public void loadMilestones(List<Milestone> ghMilestones){
		Platform.runLater(()->{
			changeMilestones(CollectionUtilities.getHubTurboMilestoneList(ghMilestones));
		});
	}
	
	public void loadTurboMilestones(List<TurboMilestone> list) {
		changeMilestones(list);
	}
	
	public void updateCachedMilestones(List<Milestone> ghMilestones, String repoId){
		ArrayList<TurboMilestone> newMilestones = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
		updateCachedList(getMilestones(), newMilestones, repoId);
	}
	
	public void refresh(){
		ServiceManager.getInstance().restartModelUpdate();
		applyChangeMethods();
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
