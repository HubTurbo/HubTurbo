package model;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

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
	
	private ObservableList<TurboUser> collaborators = FXCollections.observableArrayList();
	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	private ObservableList<TurboLabel> labels = FXCollections.observableArrayList();
	private ObservableList<TurboMilestone> milestones = FXCollections.observableArrayList();
	
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
		setupModelChangeListeners();
	}
		
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
						"Timeout while loading items from github. Please check your internet connection.");
			});
			return false;
		} catch(UnknownHostException e){
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("No Internet Connection", 
						"Please check your internet connection and try again");
			});
			return false;
		}
	}
	
	public void forceReloadComponents() throws IOException{
		HashMap<String, List> items =  ServiceManager.getInstance().getGitHubResources();
		loadComponents(repoId, items);
	}

	@SuppressWarnings("rawtypes")
	public void loadComponents(IRepositoryIdProvider repoId, HashMap<String, List> resources){
		this.repoId = repoId;
		cachedGithubComments = new ConcurrentHashMap<Integer, List<Comment>>();
		boolean isTurboResource = false;
		boolean isPublicRepo = false;
		
		// This is made with the assumption that labels of repos will not be empty (even a fresh copy of a repo)
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadTurboResources(HashMap<String, List> turboResources) {
		Platform.runLater(()-> {
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
		});
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
	
	private void setupModelChangeListeners(){
		WeakReference<Model> selfRef = new WeakReference<>(this);
		//No need to use weak listeners because model is persistent through the lifetime of the application
		collaborators.addListener((ListChangeListener.Change<? extends TurboUser> c) ->{
			selfRef.get().applyChangeMethods();
		}); 
		issues.addListener((ListChangeListener.Change<? extends TurboIssue> c) ->{
			selfRef.get().applyChangeMethods();
		});
		labels.addListener((ListChangeListener.Change<? extends TurboLabel> c) ->{
			selfRef.get().applyChangeMethods();
		});
		milestones.addListener((ListChangeListener.Change<? extends TurboMilestone> c) ->{
			selfRef.get().applyChangeMethods();
		});
	}
	
	public void applyChangeMethods(){
		for(Runnable method : methodsOnChange){
			method.run();
		}
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
	
	public void cacheCommentsListForIssue(List<Comment> comments, int issueId){
		cachedGithubComments.put(issueId, new ArrayList<Comment>(comments));
	}
	
	public List<Comment>getCommentsListForIssue(int issueId){
		return cachedGithubComments.get(issueId);
	}
 	
	public void appendToCachedIssues(TurboIssue issue){
		issues.add(0, issue);
	}
	
	public boolean isExclusiveLabelGroup(String group){
		List<TurboLabel> labelsInGrp = labels.stream()
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
				dcHandler.writeToFile(repoId, issuesETag, collabsETag, labelsETag, milestonesETag, issueCheckTime, collaborators, labels, milestones, issues);
			}
		});
	}
		
	public void updateCachedIssue(TurboIssue issue){
		TurboIssue tIssue = getIssueWithId(issue.getId());
		if(tIssue != null){
			tIssue.copyValues(issue);
			logger.debug("Updated issue: " + issue.getId());
		}else{		
			issues.add(0, issue);
			logger.info("Added issue: " + issue.getId());
		}	
	}
	
	public void addLabel(TurboLabel label){
		Platform.runLater(()->{
			labels.add(label);
		});
	}
	
	public void deleteLabel(TurboLabel label){
		Platform.runLater(()->{
			labels.remove(label);
		});
	}
	
	public void addMilestone(TurboMilestone milestone){
		Platform.runLater(()->{
			milestones.add(milestone);
		});
	}
	
	public void deleteMilestone(TurboMilestone milestone){
		Platform.runLater(()->{
			milestones.remove(milestone);
		});
	}

	public int getIndexOfIssue(int id){
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
		
		for(int i = 0; i < issues.size(); i++){
			TurboIssue issue = issues.get(i);
			if(issue.getId() == id){
				return issue;
			}
		}
		
		return null;
	}
	
	public TurboLabel getLabelByGhName(String name) {
		for (int i=0; i<labels.size(); i++) {
			if (labels.get(i).toGhName().equals(name)) {
				return labels.get(i);
			}
		}
		return null;
	}
	
	public TurboMilestone getMilestoneByTitle(String title) {
		for (int i=0; i<milestones.size(); i++) {
			if (milestones.get(i).getTitle().equals(title)) {
				return milestones.get(i);
			}
		}
		return null;
	}
	
	public TurboUser getUserByGhName(String name) {
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
	        	list.removeAll(removed);
  	
	        	Listable listItem = (Listable)newList.get(0);
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
					if(index != -1){
						Listable old = (Listable)list.get(index);
						old.copyValues(item);
					}else{
						buffer.add(item);
					}
	        	}
	        	list.addAll(buffer);
	        	
	        	dcHandler.writeToFile(repoId, issuesETag, collabsETag, labelsETag, milestonesETag, issueCheckTime, collaborators, labels, milestones, issues);
	        }

			private void logNumOfUpdates(List newList, String type) {
				logger.info("Retrieved " + newList.size() + " updated " + type + " since last sync");
			}
	   });
	}
	
	public void loadCollaborators(List<User> ghCollaborators) {	
		Platform.runLater(()->{
			collaborators.clear();
			collaborators.addAll(CollectionUtilities.getHubTurboUserList(ghCollaborators));
		});
	}
	
	public void clearCollaborators() {	
		Platform.runLater(()->{
			collaborators.clear();
		});
	}
	
	public void loadTurboCollaborators(List<TurboUser> list) {
		collaborators.clear();
		collaborators.addAll(list);
	}
	
	public void updateCachedCollaborators(List<User> ghCollaborators, String repoId){
		ArrayList<TurboUser> newCollaborators = CollectionUtilities.getHubTurboUserList(ghCollaborators);
		updateCachedList(collaborators, newCollaborators, repoId);
	}
	
	public void loadIssues(List<Issue> ghIssues) {
		if (ghIssues != null) {
			//enforceStatusStateConsistency(ghIssues);
		}
		Platform.runLater(()->{
			issues.clear();
			// Add the issues to a temporary list to prevent a quadratic number
			// of updates to subscribers of the ObservableList
			ArrayList<TurboIssue> buffer = CollectionUtilities.getHubTurboIssueList(ghIssues);
			// Add them all at once, so this hopefully propagates only one change
			issues.addAll(buffer);
			dcHandler.writeToFile(repoId.toString(), issuesETag, collabsETag, labelsETag, milestonesETag, issueCheckTime, collaborators, labels, milestones, issues);
		});
	}
	
	public void loadTurboIssues(List<TurboIssue> list) {
		issues.clear();
		issues.addAll(list);
	}

	public void loadLabels(List<Label> ghLabels){
		Platform.runLater(()->{
			labels.clear();
			ArrayList<TurboLabel> buffer = CollectionUtilities.getHubTurboLabelList(ghLabels);
			labels.addAll(buffer);
		});
	}
	
	public void loadTurboLabels(List<TurboLabel> list) {
		labels.clear();
		labels.addAll(list);
	}
	
	public void updateCachedLabels(List<Label> ghLabels, String repoId){
		ArrayList<TurboLabel> newLabels = CollectionUtilities.getHubTurboLabelList(ghLabels);
		updateCachedList(labels, newLabels, repoId);	
	}
	
	public void loadMilestones(List<Milestone> ghMilestones){
		Platform.runLater(()->{
			milestones.clear();
			ArrayList<TurboMilestone> buffer = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
			milestones.addAll(buffer);
		});
	}
	
	public void loadTurboMilestones(List<TurboMilestone> list) {
		milestones.clear();
		milestones.addAll(list);
	}
	
	public void updateCachedMilestones(List<Milestone> ghMilestones, String repoId){
		ArrayList<TurboMilestone> newMilestones = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
		updateCachedList(milestones, newMilestones, repoId);
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
