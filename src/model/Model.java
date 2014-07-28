package model;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
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

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

import service.ServiceManager;
import util.CollectionUtilities;
import util.ConfigFileHandler;
import util.ProjectConfigurations;


public class Model {
	
	private static final String CHARSET = "ISO-8859-1";
	public static final String STATE_ALL = "all";
	public static final String STATE_OPEN = "open";
	public static final String STATE_CLOSED = "closed";
	
	private ObservableList<TurboUser> collaborators = FXCollections.observableArrayList();
	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	private ObservableList<TurboLabel> labels = FXCollections.observableArrayList();
	private ObservableList<TurboMilestone> milestones = FXCollections.observableArrayList();
	
	private ConcurrentHashMap<Integer, List<Comment>> cachedGithubComments = new ConcurrentHashMap<Integer, List<Comment>>();
	
	private ArrayList<Runnable> methodsOnChange = new ArrayList<Runnable>();
	
	protected IRepositoryIdProvider repoId;
			
	public Model(){
		setupModelChangeListeners();
	}
		
	public IRepositoryIdProvider getRepoId(){
		return repoId;
	}
	
	public void setRepoId(IRepositoryIdProvider repoId) {
		this.repoId = repoId;
	}
	
	public void loadComponents(IRepositoryIdProvider repoId) throws IOException{
		this.repoId = repoId;
		ConfigFileHandler.loadProjectConfig(getRepoId());
		cachedGithubComments = new ConcurrentHashMap<Integer, List<Comment>>();
		loadCollaborators(ServiceManager.getInstance().getCollaborators());
		loadLabels(ServiceManager.getInstance().getLabels());
		loadMilestones(ServiceManager.getInstance().getMilestones());
		loadIssues(ServiceManager.getInstance().getAllIssues());
	}
	
	@SuppressWarnings("unchecked")
	public void loadComponents(IRepositoryIdProvider repoId, HashMap<String, List> ghResources) throws IOException{
		this.repoId = repoId;
		ConfigFileHandler.loadProjectConfig(getRepoId());
		cachedGithubComments = new ConcurrentHashMap<Integer, List<Comment>>();
		loadCollaborators((List<User>) ghResources.get(ServiceManager.KEY_COLLABORATORS));
		loadLabels((List<Label>) ghResources.get(ServiceManager.KEY_LABELS));
		loadMilestones((List<Milestone>) ghResources.get(ServiceManager.KEY_MILESTONES));
		loadIssues((List<Issue>)ghResources.get(ServiceManager.KEY_ISSUES));
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

	}
		
	public void updateCachedIssue(TurboIssue issue){
		TurboIssue tIssue = getIssueWithId(issue.getId());
		if(tIssue != null){
			tIssue.copyValues(issue);
		}else{
			issues.add(0, issue);
		}
	}
	
	public TurboLabel createLabel(TurboLabel newLabel) {
		Label ghLabel = newLabel.toGhResource();
		Label createdLabel = null;
		try {
			createdLabel = ServiceManager.getInstance().createLabel(ghLabel);
		} catch (IOException e) {
			e.printStackTrace();
		}
		TurboLabel returnedLabel = new TurboLabel(createdLabel);
		labels.add(returnedLabel);
		refresh();
		return returnedLabel;
	}
	
	public TurboMilestone createMilestone(TurboMilestone newMilestone) {
		Milestone ghMilestone = newMilestone.toGhResource();
		Milestone createdMilestone = null;
		try {
			createdMilestone = ServiceManager.getInstance().createMilestone(ghMilestone);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		TurboMilestone returnedMilestone = new TurboMilestone(createdMilestone);
		milestones.add(returnedMilestone);
		refresh();
		return returnedMilestone;
	}
	
	public void deleteLabel(TurboLabel label) {
		try {
			ServiceManager.getInstance().deleteLabel(URLEncoder.encode(label.toGhName(), CHARSET));
			labels.remove(label);
			refresh();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteMilestone(TurboMilestone milestone) {
		try {
			ServiceManager.getInstance().deleteMilestone(milestone.getNumber());
			milestones.remove(milestone);
			refresh();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	public void updateIssue(TurboIssue originalIssue, TurboIssue editedIssue) {
//		TurboIssueEdit command = new TurboIssueEdit(this, originalIssue, editedIssue);
//		command.execute();
//	}
	
	
	public void updateLabel(TurboLabel editedLabel, String labelName) {
		Label ghLabel = editedLabel.toGhResource();
		try {
			ServiceManager.getInstance().editLabel(ghLabel, URLEncoder.encode(labelName, CHARSET));
			refresh();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateMilestone(TurboMilestone editedMilestone) {
		Milestone ghMilestone = editedMilestone.toGhResource();
		try {
			ServiceManager.getInstance().editMilestone(ghMilestone);
			refresh();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		collaborators.clear();
		collaborators.addAll(CollectionUtilities.getHubTurboUserList(ghCollaborators));
	}
	
	public void updateCachedCollaborators(List<User> ghCollaborators){
		ArrayList<TurboUser> newCollaborators = CollectionUtilities.getHubTurboUserList(ghCollaborators);
		updateCachedList(collaborators, newCollaborators);
	}
	
	public void loadIssues(List<Issue> ghIssues) {
		issues.clear();
		enforceStatusStateConsistency(ghIssues);
		// Add the issues to a temporary list to prevent a quadratic number
		// of updates to subscribers of the ObservableList
		ArrayList<TurboIssue> buffer = CollectionUtilities.getHubTurboIssueList(ghIssues);
		// Add them all at once, so this hopefully propagates only one change
		
		issues.addAll(buffer);
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private boolean isInconsistent(String state, String ghLabelName) {
		return ((ProjectConfigurations.isOpenStatusLabel(ghLabelName) && state.equals(STATE_CLOSED)) ||
				(ProjectConfigurations.isClosedStatusLabel(ghLabelName) && state.equals(STATE_OPEN)));
	}

	public void loadLabels(List<Label> ghLabels){
		standardiseStatusLabels(ghLabels);
		labels.clear();
		ArrayList<TurboLabel> buffer = CollectionUtilities.getHubTurboLabelList(ghLabels);
		labels.addAll(buffer);
	}
	
	private void standardiseStatusLabels(List<Label> ghLabels) {
		List<String> defaultStatuses = ProjectConfigurations.getStatusLabels();
		List<String> projectLabels =
				new ArrayList<String>(ghLabels.stream()
				.map(label -> label.getName())
				.collect(Collectors.toList()));
		
		defaultStatuses.removeAll(projectLabels);
		for (String standardStatus : defaultStatuses) {
			Label statusLabel = new Label();
			statusLabel.setName(standardStatus);
			if (ProjectConfigurations.isOpenStatusLabel(standardStatus)) {
				statusLabel.setColor("009800");
			} else {
				statusLabel.setColor("0052cc");
			}
			try {
				ghLabels.add(ServiceManager.getInstance().createLabel(statusLabel));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public void updateCachedLabels(List<Label> ghLabels){
		ArrayList<TurboLabel> newLabels = CollectionUtilities.getHubTurboLabelList(ghLabels);
		updateCachedList(labels, newLabels);
	}
	
	public void loadMilestones(List<Milestone> ghMilestones){
		milestones.clear();
		ArrayList<TurboMilestone> buffer = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
		milestones.addAll(buffer);
	}
	
	public void updateCachedMilestones(List<Milestone> ghMilestones){
		ArrayList<TurboMilestone> newMilestones = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
		updateCachedList(milestones, newMilestones);
	}
	
	public void refresh(){
		ServiceManager.getInstance().restartModelUpdate();
		applyChangeMethods();
	}
}
