package model;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.RequestException;

import service.ServiceManager;
import storage.DataCacheFileHandler;
import storage.DataManager;
import ui.StatusBar;
import storage.TurboRepoData;
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
	private static final String MESSAGE_LOADING_PROJECT_CONFIG = "Loading project configuration...";
	
	private ObservableList<TurboUser> collaborators = FXCollections.observableArrayList();
	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	private ObservableList<TurboLabel> labels = FXCollections.observableArrayList();
	private ObservableList<TurboMilestone> milestones = FXCollections.observableArrayList();
	
	private ConcurrentHashMap<Integer, List<Comment>> cachedGithubComments = new ConcurrentHashMap<Integer, List<Comment>>();
	
	private ArrayList<Runnable> methodsOnChange = new ArrayList<Runnable>();
	
	private TurboRepoData repo;
	protected IRepositoryIdProvider repoId;
	
	private String issuesETag = null;
	private String collabsETag = null;
	private String labelsETag = null;
	private String milestonesETag = null;
			
	public Model(){
		setupModelChangeListeners();
	}
		
	public IRepositoryIdProvider getRepoId(){
		return repoId;
	}
	
	public void setRepoId(IRepositoryIdProvider repoId) {
		this.repoId = repoId;
		repo = DataCacheFileHandler.getInstance().getRepoGivenId(repoId.toString());
	}
	
	@SuppressWarnings("rawtypes")
	public boolean loadComponents(IRepositoryIdProvider repoId) throws IOException{
		try{
			HashMap<String, List> items =  ServiceManager.getInstance().getGitHubResources(repoId);
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void loadComponents(IRepositoryIdProvider repoId, HashMap<String, List> ghResources){
		this.repoId = repoId;
		StatusBar.displayMessage(MESSAGE_LOADING_PROJECT_CONFIG);
		DataManager.getInstance().loadProjectConfig(getRepoId());
		cachedGithubComments = new ConcurrentHashMap<Integer, List<Comment>>();
		boolean isTurboResource = false;
		if (ghResources.get(ServiceManager.KEY_COLLABORATORS) != null) {
			if (ghResources.get(ServiceManager.KEY_COLLABORATORS).get(0).getClass() == TurboUser.class) {
				isTurboResource = true;
			}
		}
		WeakReference<Model> selfRef = new WeakReference<>(this);
		if (isTurboResource) {
			StatusBar.displayMessage(MESSAGE_LOADING_COLLABS);
			loadTurboCollaborators((List<TurboUser>) ghResources.get(ServiceManager.KEY_COLLABORATORS));
			StatusBar.displayMessage(MESSAGE_LOADING_LABELS);
			loadTurboLabels((List<TurboLabel>) ghResources.get(ServiceManager.KEY_LABELS));
			StatusBar.displayMessage(MESSAGE_LOADING_MILESTONES);
			loadTurboMilestones((List<TurboMilestone>) ghResources.get(ServiceManager.KEY_MILESTONES));
			
			// only get issues now to prevent assertion error in getLabelReference of TurboIssues
			List<TurboIssue> issues = repo.getIssues(ServiceManager.getInstance().getModel());
			StatusBar.displayMessage(MESSAGE_LOADING_ISSUES);
			loadTurboIssues(issues);
		} else {
			StatusBar.displayMessage(MESSAGE_LOADING_COLLABS);
			loadCollaborators((List<User>) ghResources.get(ServiceManager.KEY_COLLABORATORS));
			StatusBar.displayMessage(MESSAGE_LOADING_LABELS);
			loadLabels((List<Label>) ghResources.get(ServiceManager.KEY_LABELS));
			StatusBar.displayMessage(MESSAGE_LOADING_MILESTONES);
			loadMilestones((List<Milestone>) ghResources.get(ServiceManager.KEY_MILESTONES));
			StatusBar.displayMessage(MESSAGE_LOADING_ISSUES);
			loadIssues((List<Issue>)ghResources.get(ServiceManager.KEY_ISSUES));
		}
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

	public void updateCachedIssues(List<Issue> issueList){
		if(issueList.size() == 0){
			return;
		} else {
			enforceStatusStateConsistency(issueList);
		}
		WeakReference<Model> selfRef = new WeakReference<Model>(this);
		for(int i = issueList.size() - 1; i >= 0; i--){
			Issue issue = issueList.get(i);
			Platform.runLater(new Runnable() {
		        @Override
		        public void run() {
		        	TurboIssue newCached = new TurboIssue(issue, selfRef.get());
		        	updateCachedIssue(newCached);
		        }
		   });
		}
		DataCacheFileHandler.getInstance().writeToFile(repoId.toString(), issuesETag, collabsETag, labelsETag, milestonesETag, collaborators, labels, milestones, issues);
	}
		
	public void updateCachedIssue(TurboIssue issue){
		TurboIssue tIssue = getIssueWithId(issue.getId());
		if(tIssue != null){
			tIssue.copyValues(issue);
		}else{
			issues.add(0, issue);
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
	private void updateCachedList(List list, List newList){
		HashMap<String, HashSet> changes = CollectionUtilities.getChangesToList(list, newList);
		HashSet removed = changes.get(CollectionUtilities.REMOVED_TAG);
		Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	list.removeAll(removed);
	        	newList.stream()
	        	       .forEachOrdered(item -> updateCachedListItem((Listable)item, list));
	        }
	   });
	}
	
	@SuppressWarnings("unchecked")
	private void updateCachedListItem(Listable updated, @SuppressWarnings("rawtypes") List list){
		int index = list.indexOf(updated);
		if(index != -1){
			Listable old = (Listable)list.get(index);
			old.copyValues(updated);
		}else{
			list.add(updated);
		}
	}
	
	public void loadCollaborators(List<User> ghCollaborators) {	
		Platform.runLater(()->{
			collaborators.clear();
			collaborators.addAll(CollectionUtilities.getHubTurboUserList(ghCollaborators));
		});
	}
	
	public void loadTurboCollaborators(List<TurboUser> list) {
		Platform.runLater(()->{
			collaborators.clear();
			collaborators.addAll(list);
		});
	}
	
	public void updateCachedCollaborators(List<User> ghCollaborators){
		ArrayList<TurboUser> newCollaborators = CollectionUtilities.getHubTurboUserList(ghCollaborators);
		updateCachedList(collaborators, newCollaborators);
		DataCacheFileHandler.getInstance().writeToFile(repoId.toString(), issuesETag, collabsETag, labelsETag, milestonesETag, collaborators, labels, milestones, issues);
	}
	
	public void loadIssues(List<Issue> ghIssues) {
		if (ghIssues != null) {
			enforceStatusStateConsistency(ghIssues);
		}
		Platform.runLater(()->{
			issues.clear();
			// Add the issues to a temporary list to prevent a quadratic number
			// of updates to subscribers of the ObservableList
			ArrayList<TurboIssue> buffer = CollectionUtilities.getHubTurboIssueList(ghIssues);
			// Add them all at once, so this hopefully propagates only one change
			issues.addAll(buffer);
			
			DataCacheFileHandler.getInstance().writeToFile(repoId.toString(), issuesETag, collabsETag, labelsETag, milestonesETag, collaborators, labels, milestones, issues);
		});
	}
	
	public void loadTurboIssues(List<TurboIssue> list) {
		Platform.runLater(()->{
			issues.clear();
			issues.addAll(list);
		});
	}

	private void enforceStatusStateConsistency(List<Issue> ghIssues) {
		for (Issue ghIssue : ghIssues) {
			Set<Label> toBeRemovedLabels = new HashSet<Label>();
			for (Label ghLabel : ghIssue.getLabels()) {
				if (isInconsistent(ghIssue.getState(), ghLabel.getName())) {
					toBeRemovedLabels.add(ghLabel);
				}
			}
			ghIssue.getLabels().removeAll(toBeRemovedLabels);
			
			if (!toBeRemovedLabels.isEmpty()) {
				try {
					ServiceManager.getInstance().setLabelsForIssue(ghIssue.getNumber(), ghIssue.getLabels());
				} catch (IOException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	private boolean isInconsistent(String state, String ghLabelName) {
		DataManager dataManager = DataManager.getInstance();
		return ((dataManager.isOpenStatusLabel(ghLabelName) && state.equals(STATE_CLOSED)) ||
				(dataManager.isClosedStatusLabel(ghLabelName) && state.equals(STATE_OPEN)));
	}

	public void loadLabels(List<Label> ghLabels){
		standardiseStatusLabels(ghLabels);
		Platform.runLater(()->{
			labels.clear();
			ArrayList<TurboLabel> buffer = CollectionUtilities.getHubTurboLabelList(ghLabels);
			labels.addAll(buffer);
		});
	}
	
	public void loadTurboLabels(List<TurboLabel> list) {
		Platform.runLater(()->{
			labels.clear();
			labels.addAll(list);
		});
	}
	
	private void standardiseStatusLabels(List<Label> ghLabels) {
		DataManager dataManager = DataManager.getInstance();
		List<String> defaultStatuses = dataManager.getStatusLabels();
		List<String> projectLabels = ghLabels.stream()
											 .map(label -> label.getName())
											 .collect(Collectors.toList());
		
		defaultStatuses.removeAll(projectLabels);

		for (String standardStatus : defaultStatuses) {
			if(standardStatus == null){
				//Check is required because status labels array serialised from json file can contain null
				continue;
			}
			Label statusLabel = new Label();
			statusLabel.setName(standardStatus);
			if (dataManager.isOpenStatusLabel(standardStatus)) {
				statusLabel.setColor("009800");
			} else {
				statusLabel.setColor("0052cc");
			}
			try {
				ghLabels.add(ServiceManager.getInstance().createLabel(statusLabel));
			} catch (IOException e) {
				if(e instanceof RequestException){
					//Happens because user has no repo permissions
					if(((RequestException) e).getStatus() == 404){
						logger.error("No repository permissions to create label", e);
						break;
					}
				}else{
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		}
		
	}

	public void updateCachedLabels(List<Label> ghLabels){
		ArrayList<TurboLabel> newLabels = CollectionUtilities.getHubTurboLabelList(ghLabels);
		updateCachedList(labels, newLabels);
		DataCacheFileHandler.getInstance().writeToFile(repoId.toString(), issuesETag, collabsETag, labelsETag, milestonesETag, collaborators, labels, milestones, issues);
	}
	
	public void loadMilestones(List<Milestone> ghMilestones){
		Platform.runLater(()->{
			milestones.clear();
			ArrayList<TurboMilestone> buffer = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
			milestones.addAll(buffer);
		});
	}
	
	public void loadTurboMilestones(List<TurboMilestone> list) {
		Platform.runLater(()->{
			milestones.clear();
			milestones.addAll(list);
		});
	}
	
	public void updateCachedMilestones(List<Milestone> ghMilestones){
		ArrayList<TurboMilestone> newMilestones = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
		updateCachedList(milestones, newMilestones);
		DataCacheFileHandler.getInstance().writeToFile(repoId.toString(), issuesETag, collabsETag, labelsETag, milestonesETag, collaborators, labels, milestones, issues);
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
}
