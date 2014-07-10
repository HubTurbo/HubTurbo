package model;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.MilestoneService;

import service.GitHubClientExtended;
import service.IssueServiceExtended;
import service.LabelServiceFixed;
import util.CollectionUtilities;
import util.ConfigFileHandler;
import util.DialogMessage;
import util.UserConfigurations;

public class Model {
	
	private static final String CHARSET = "ISO-8859-1";
	public static final String STATE_ALL = "all";
	public static final String STATE_OPEN = "open";
	public static final String STATE_CLOSED = "closed";
	
	private ObservableList<TurboUser> collaborators = FXCollections.observableArrayList();
	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	private ObservableList<TurboLabel> labels = FXCollections.observableArrayList();
	private ObservableList<TurboMilestone> milestones = FXCollections.observableArrayList();

	private IRepositoryIdProvider repoId;
	private UserConfigurations config = ConfigFileHandler.loadConfig();
	
	private CollaboratorService collabService;
	private IssueServiceExtended issueService;
	private LabelServiceFixed labelService;
	private MilestoneService milestoneService;
	
	public Model(GitHubClientExtended ghClient) {
		this.collabService = new CollaboratorService(ghClient);
		this.issueService = new IssueServiceExtended(ghClient);
		this.labelService = new LabelServiceFixed(ghClient);
		this.milestoneService = new MilestoneService(ghClient);
	}
	
	public void setRepoId(String owner, String name) {
		repoId = RepositoryId.create(owner, name);
		loadCollaborators();
		loadLabels();
		loadMilestones();
		loadIssues();
	}

	public void processInheritedLabels(TurboIssue issue, List<Integer> originalParents) {
		List<Integer> editedParents = issue.getParents();
		HashMap<String, HashSet<Integer>> changeSet = CollectionUtilities.getChangesToList(originalParents, editedParents);
		HashSet<Integer> removed = changeSet.get(CollectionUtilities.REMOVED_TAG);
		HashSet<Integer> added = changeSet.get(CollectionUtilities.ADDED_TAG);
		
		removeInheritedLabels(removed, issue);
		addInheritedLabels(added, issue);

	}
	
	private void addInheritedLabels(HashSet<Integer> addedParents, TurboIssue issue){
		for (Integer addedParentId : addedParents) {
			TurboIssue addedParent = getIssueWithId(addedParentId);
			if(addedParent == null){
				continue;
			}
			for(TurboLabel label : addedParent.getLabels()){
				if(!UserConfigurations.isExcludedLabel(label.toGhName())){
					issue.addLabel(label);
				}
			}
		}
	}
	
	private void removeInheritedLabels(HashSet<Integer> removedParents, TurboIssue issue){
		List<Integer> editedParents = issue.getParents();
		for (Integer removedParentId : removedParents) {
			TurboIssue removedParent = getIssueWithId(removedParentId);
			if(removedParent == null){
				continue;
			}
			for (TurboLabel label : removedParent.getLabels()) {
				if(UserConfigurations.isExcludedLabel(label.toGhName())){
					continue;
				}
				boolean toBeRemoved = true;
				// Loop to check if other parents have the label to be removed
				for (Integer editedParentId : editedParents) {
					TurboIssue editedParent = getIssueWithId(editedParentId);
					if (editedParent.hasLabel(label)) {
						toBeRemoved = false;
						break;
					}
				}
				
				if (toBeRemoved) {
					issue.removeLabel(label);
				}
			}
		}
	}

	public IRepositoryIdProvider getRepoId(){
		return repoId;
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
	
	public TurboIssue createIssue(TurboIssue newIssue) {
		Issue ghIssue = newIssue.toGhResource();
		Issue createdIssue = null;
		try {
			createdIssue = issueService.createIssue(repoId, ghIssue);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		TurboIssue returnedIssue = new TurboIssue(createdIssue, this);
		issues.add(0, returnedIssue);
		return returnedIssue;
	}
	
	public void updateCachedIssues(List<Issue> issueList){
		if(issueList.size() == 0){
			return;
		}
		
		for(Issue issue: issueList){
			Platform.runLater(new Runnable() {
		        @Override
		        public void run() {
		        	updateCachedIssue(issue);
		        }
		   });
		}

	}
	
	private void updateCachedIssue(Issue issue){
		TurboIssue newCached = new TurboIssue(issue, this);
//		int index = getIndexOfIssue(issue.getNumber());
		TurboIssue tIssue = getIssueWithId(issue.getNumber());
		if(tIssue != null){
//			issues.set(index, newCached);
			tIssue.copyValues(newCached);
		}else{
			issues.add(0, newCached);
		}
	}
	
	public TurboLabel createLabel(TurboLabel newLabel) {
		Label ghLabel = newLabel.toGhResource();
		Label createdLabel = null;
		try {
			createdLabel = labelService.createLabel(repoId, ghLabel);
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
			createdMilestone = milestoneService.createMilestone(repoId, ghMilestone);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		TurboMilestone returnedMilestone = new TurboMilestone(createdMilestone);
		milestones.add(returnedMilestone);
		return returnedMilestone;
	}
	
	public void deleteLabel(TurboLabel label) {
		try {
			labelService.deleteLabel(repoId, URLEncoder.encode(label.toGhName(), CHARSET));
			labels.remove(label);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteMilestone(TurboMilestone milestone) {
		try {
			milestoneService.deleteMilestone(repoId, milestone.getNumber());
			milestones.remove(milestone);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateIssue(TurboIssue originalIssue, TurboIssue editedIssue) {
		try {
			int issueId = editedIssue.getId();
			StringBuilder changeLog = new StringBuilder();
			HashMap<String, Object> issueQuery =  issueService.getIssueData(repoId, issueId);
			String dateModified = (String) issueQuery.get(IssueServiceExtended.ISSUE_DATE);
			TurboIssue latestIssue = new TurboIssue((Issue)issueQuery.get(IssueServiceExtended.ISSUE_CONTENTS), this);
			
			boolean descUpdated = editedIssue.mergeIssues(originalIssue, latestIssue, changeLog);
			//TODO: inform user when description update failed
			Issue latest = latestIssue.toGhResource();
			issueService.editIssue(repoId, latest, dateModified);
			if(changeLog.length() > 0){
				issueService.createComment(repoId, ""+issueId, changeLog.toString());
			}
			updateCachedIssue(latest);
			if(!descUpdated){
				DialogMessage.showWarningDialog("Issue description not updated", "The issue description has been concurrently modified. "
						+ "Please refresh and enter your descripton again.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	
	public void updateLabel(TurboLabel editedLabel, String labelName) {
		Label ghLabel = editedLabel.toGhResource();
		try {
			labelService.editLabel(repoId, ghLabel, URLEncoder.encode(labelName, CHARSET));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateMilestone(TurboMilestone editedMilestone) {
		Milestone ghMilestone = editedMilestone.toGhResource();
		try {
			milestoneService.editMilestone(repoId, ghMilestone);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int getIndexOfIssue(int id){
		for(int i = 0; i < issues.size(); i++){
			if(((TurboIssue)(issues.get(i))).getId() == id){
				return i;
			}
		}
		return -1;
	}
	
	private TurboIssue getIssueWithId(int id){
		for(int i = 0; i < issues.size(); i++){
			TurboIssue issue = issues.get(i);
			if(issue.getId() == id){
				return issue;
			}
		}
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateCachedList(List list, List newList){
		HashMap<String, HashSet> changes = CollectionUtilities.getChangesToList(list, newList);
		HashSet removed = changes.get(CollectionUtilities.REMOVED_TAG);
		list.removeAll(removed);
		for(Object item: newList){
			updateCachedListItem((Listable)item, list);
		}
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
			List<User> ghCollaborators = collabService.getCollaborators(repoId);
			setCachedCollaborators(ghCollaborators);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void updateCachedCollaborators(List<User> ghCollaborators){
		ArrayList<Listable> newCollaborators = new ArrayList<Listable>();
		for(User ghCollaborator : ghCollaborators) {
//			updateCachedListItem(new TurboUser(ghCollaborator), collaborators);
			newCollaborators.add(new TurboUser(ghCollaborator));
		}
		updateCachedList(collaborators, newCollaborators);
	}
	
	private void setCachedCollaborators(List<User> ghCollaborators){
		collaborators.clear();
		for(User ghCollaborator : ghCollaborators) {
			collaborators.add(new TurboUser(ghCollaborator));
		}
	}
	
	private boolean loadIssues() {
		issues.clear();
		Map<String, String> filters = new HashMap<String, String>();
		filters.put(IssueService.FIELD_FILTER, STATE_ALL);
		filters.put(IssueService.FILTER_STATE, STATE_ALL);
		try {		
			List<Issue> ghIssues = issueService.getIssues(repoId, filters);
			
			// Add the issues to a temporary list to prevent a quadratic number
			// of updates to subscribers of the ObservableList
			ArrayList<TurboIssue> buffer = new ArrayList<>();
			for (Issue ghIssue : ghIssues) {
				buffer.add(new TurboIssue(ghIssue, this));
			}
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
			List<Label> ghLabels = labelService.getLabels(repoId);
			standardiseStatusLabels(ghLabels);
			setCachedLabels(ghLabels);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void standardiseStatusLabels(List<Label> ghLabels) {
		List<String> standardStatuses =new ArrayList<String>();
		standardStatuses.add("status.new");
		standardStatuses.add("status.accepted");
		standardStatuses.add("status.started");
		standardStatuses.add("status.fixed");
		standardStatuses.add("status.verified");
		standardStatuses.add("status.invalid");
		standardStatuses.add("status.duplicate");
		standardStatuses.add("status.wontfix");
		standardStatuses.add("status.done");
		List<String> projectLabels = new ArrayList<String>();
		for (Label label : ghLabels) {
			projectLabels.add(label.getName());
		}
		standardStatuses.removeAll(projectLabels);
		for (String standardStatus : standardStatuses) {
			Label statusLabel = new Label();
			statusLabel.setName(standardStatus);
			if (standardStatus.endsWith("new") ||
				standardStatus.endsWith("accepted") ||
				standardStatus.endsWith("started")) {
				statusLabel.setColor("009800");
			} else {
				statusLabel.setColor("0052cc");
			}
			try {
				ghLabels.add(labelService.createLabel(repoId, statusLabel));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public void updateCachedLabels(List<Label> ghLabels){
		ArrayList<TurboLabel> newLabels = new ArrayList<TurboLabel>();
		for (Label ghLabel : ghLabels) {
//			updateCachedListItem(new TurboLabel(ghLabel), labels);
			newLabels.add(new TurboLabel(ghLabel));
		}
		updateCachedList(labels, newLabels);
	}
	
	private void setCachedLabels(List<Label> ghLabels){
		labels.clear();
		// See loadIssues for why this buffer list is needed
		ArrayList<TurboLabel> buffer = new ArrayList<>();
		for (Label ghLabel : ghLabels) {
			buffer.add(new TurboLabel(ghLabel));
		}
		labels.addAll(buffer);
	}
	
	private boolean loadMilestones(){
		try {		
			List<Milestone> ghMilestones = milestoneService.getMilestones(repoId, STATE_ALL);
			setCachedMilestones(ghMilestones);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void updateCachedMilestones(List<Milestone> ghMilestones){
		ArrayList<TurboMilestone> newMilestones = new ArrayList<TurboMilestone>();
		for (Milestone ghMilestone : ghMilestones) {
//			updateCachedListItem(new TurboMilestone(ghMilestone), milestones);
			newMilestones.add(new TurboMilestone(ghMilestone));
		}
		updateCachedList(milestones, newMilestones);
	}
	
	private void setCachedMilestones(List<Milestone> ghMilestones){
		milestones.clear();
		// See loadIssues for why this buffer list is needed
		ArrayList<TurboMilestone> buffer = new ArrayList<>();
		for (Milestone ghMilestone : ghMilestones) {
			buffer.add(new TurboMilestone(ghMilestone));
		}
		milestones.addAll(buffer);
	}
}
