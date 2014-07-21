package model;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

import service.ServiceManager;
import util.CollectionUtilities;
import util.ConfigFileHandler;
import util.Defaults;
import util.UserConfigurations;

import command.TurboIssueEdit;


public class Model {
	
	private static final String CHARSET = "ISO-8859-1";
	public static final String STATE_ALL = "all";
	public static final String STATE_OPEN = "open";
	public static final String STATE_CLOSED = "closed";
	
	private ObservableList<TurboUser> collaborators = FXCollections.observableArrayList();
	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	private ObservableList<TurboLabel> labels = FXCollections.observableArrayList();
	private ObservableList<TurboMilestone> milestones = FXCollections.observableArrayList();
	
	protected IRepositoryIdProvider repoId;

	private UserConfigurations config = ConfigFileHandler.loadUserConfig();
	
	public Model(){
		
	}
	
	public IRepositoryIdProvider getRepoId(){
		return repoId;
	}
	
	public void loadComponents(IRepositoryIdProvider repoId){
		this.repoId = repoId;
		loadCollaborators();
		loadLabels();
		loadMilestones();
		loadIssues();
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
	
	public void appendToCachedIssues(TurboIssue issue){
		issues.add(0, issue);
	}
	
	public TurboIssue createIssue(TurboIssue newIssue) {
		Issue ghIssue = newIssue.toGhResource();
		Issue createdIssue = null;
		try {
			createdIssue = ServiceManager.getInstance().createIssue(ghIssue);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		TurboIssue returnedIssue = new TurboIssue(createdIssue, this);
		appendToCachedIssues(returnedIssue);
		return returnedIssue;
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
		return returnedMilestone;
	}
	
	public void deleteLabel(TurboLabel label) {
		try {
			ServiceManager.getInstance().deleteLabel(URLEncoder.encode(label.toGhName(), CHARSET));
			labels.remove(label);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteMilestone(TurboMilestone milestone) {
		try {
			ServiceManager.getInstance().deleteMilestone(milestone.getNumber());
			milestones.remove(milestone);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateIssue(TurboIssue originalIssue, TurboIssue editedIssue) {
		TurboIssueEdit command = new TurboIssueEdit(this, originalIssue, editedIssue);
		command.execute();
	}
	
	
	public void updateLabel(TurboLabel editedLabel, String labelName) {
		Label ghLabel = editedLabel.toGhResource();
		try {
			ServiceManager.getInstance().editLabel(ghLabel, URLEncoder.encode(labelName, CHARSET));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateMilestone(TurboMilestone editedMilestone) {
		Milestone ghMilestone = editedMilestone.toGhResource();
		try {
			ServiceManager.getInstance().editMilestone(ghMilestone);
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
	
	private boolean loadCollaborators() {	
		try {
			List<User> ghCollaborators = ServiceManager.getInstance().getCollaborators();
			setCachedCollaborators(ghCollaborators);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void updateCachedCollaborators(List<User> ghCollaborators){
		ArrayList<TurboUser> newCollaborators = CollectionUtilities.getHubTurboUserList(ghCollaborators);
		updateCachedList(collaborators, newCollaborators);
	}
	
	private void setCachedCollaborators(List<User> ghCollaborators){
		collaborators.clear();
		collaborators.addAll(CollectionUtilities.getHubTurboUserList(ghCollaborators));
	}
	
	private boolean loadIssues() {
		issues.clear();
		try {		
			List<Issue> ghIssues = ServiceManager.getInstance().getAllIssues();
			// Add the issues to a temporary list to prevent a quadratic number
			// of updates to subscribers of the ObservableList
			ArrayList<TurboIssue> buffer = CollectionUtilities.getHubTurboIssueList(ghIssues);
			// Add them all at once, so this hopefully propagates only one change
			issues.addAll(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean loadLabels(){
		try {
			List<Label> ghLabels = ServiceManager.getInstance().getLabels();
			standardiseStatusLabels(ghLabels);
			setCachedLabels(ghLabels);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void standardiseStatusLabels(List<Label> ghLabels) {
		List<String> defaultStatuses = Defaults.getDefaultStatusLabels();
		List<String> projectLabels = new ArrayList<String>();
		
		for(Label label : ghLabels){
			projectLabels.add(label.getName());
		}
		
		defaultStatuses.removeAll(projectLabels);
		for (String standardStatus : defaultStatuses) {
			Label statusLabel = new Label();
			statusLabel.setName(standardStatus);
			if (standardStatus.endsWith(".new") ||
				standardStatus.endsWith(".accepted") ||
				standardStatus.endsWith(".started") ||
				standardStatus.endsWith(".reopened")) {
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
	
	private void setCachedLabels(List<Label> ghLabels){
		labels.clear();
		ArrayList<TurboLabel> buffer = CollectionUtilities.getHubTurboLabelList(ghLabels);
		labels.addAll(buffer);
	}
	
	private boolean loadMilestones(){
		try {		
			List<Milestone> ghMilestones = ServiceManager.getInstance().getMilestones();
			setCachedMilestones(ghMilestones);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void updateCachedMilestones(List<Milestone> ghMilestones){
		ArrayList<TurboMilestone> newMilestones = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
		updateCachedList(milestones, newMilestones);
	}
	
	private void setCachedMilestones(List<Milestone> ghMilestones){
		milestones.clear();
		ArrayList<TurboMilestone> buffer = CollectionUtilities.getHubTurboMilestoneList(ghMilestones);
		milestones.addAll(buffer);
	}
}
