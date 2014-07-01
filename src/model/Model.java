package model;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.MilestoneService;

import util.LabelServiceFixed;

public class Model {
	
	private static final String CHARSET = "ISO-8859-1";
	public static final String STATE_ALL = "all";
	public static final String STATE_OPEN = "open";
	public static final String STATE_CLOSED = "closed";
	
	private static final String REMOVED_TAG = "removed";
	private static final String ADDED_TAG = "added";
	
	private ObservableList<TurboUser> collaborators = FXCollections.observableArrayList();
	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	private ObservableList<TurboLabel> labels = FXCollections.observableArrayList();
	private ObservableList<TurboMilestone> milestones = FXCollections.observableArrayList();

	private IRepositoryIdProvider repoId;
	
	private CollaboratorService collabService;
	private IssueService issueService;
	private LabelServiceFixed labelService;
	private MilestoneService milestoneService;
	
	public Model(GitHubClient ghClient) {
		this.collabService = new CollaboratorService(ghClient);
		this.issueService = new IssueService(ghClient);
		this.labelService = new LabelServiceFixed(ghClient);
		this.milestoneService = new MilestoneService(ghClient);
	}
	
	public void setRepoId(String owner, String name) {
		repoId = RepositoryId.create(owner, name);
		loadCollaborators();
		loadIssues();
		loadLabels();
		loadMilestones();
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
		TurboIssue returnedIssue = new TurboIssue(createdIssue);
		issues.add(returnedIssue);
		return returnedIssue;
	}
	
	public void updateCachedIssues(List<Issue> issueList){
		if(issueList.size() == 0){
			return;
		}
		
		for(Issue issue: issueList){
			updateCachedIssue(issue);
		}
		//TODO: for now just reload collaborators, labels and milestones
		loadCollaborators();
		loadLabels();
		loadMilestones();
	}
	
	private void updateCachedIssue(Issue issue){
		TurboIssue newCached = new TurboIssue(issue);
		int index = getIndexOfIssue(issue.getNumber());
		if(index != -1){
//			updateIssueAtIndex(index, newCached);
			issues.set(index, newCached);
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
	
	public void updateIssue(TurboIssue orignalIssue, TurboIssue editedIssue) {
		Issue original = orignalIssue.toGhResource();
		Issue edited = editedIssue.toGhResource();
		StringBuilder changeLog = new StringBuilder();
		try {
			int issueId = editedIssue.getId();
			Issue latest = issueService.getIssue(repoId, issueId);
			mergeTitle(original, edited, latest);
			mergeBody(original, edited, latest);
			mergeAssignee(original, edited, latest, changeLog);
			mergeState(original, edited, latest, changeLog);
			mergeMilestone(original, edited, latest, changeLog);
			mergeLabels(original, edited, latest, changeLog);
			if(changeLog.length() > 0){
				issueService.createComment(repoId, ""+issueId, changeLog.toString());
			}
			issueService.editIssue(repoId, latest);
			updateCachedIssue(latest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void updateLabel(TurboLabel editedLabel, String labelName) {
		Label ghLabel = editedLabel.toGhResource();
		try {
			labelService.editLabel(repoId, ghLabel, URLEncoder.encode(labelName, CHARSET));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateMilestone(TurboMilestone editedMilestone) {
		Milestone ghMilestone = editedMilestone.toGhResource();
		try {
			milestoneService.editMilestone(repoId, ghMilestone);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void updateIssueAtIndex(int index, TurboIssue newIssue){
		TurboIssue issue = issues.get(index);
		issue.copyValues(newIssue);
	}
	
	private int getIndexOfIssue(int id){
		for(int i = 0; i < issues.size(); i++){
			if(((TurboIssue)(issues.get(i))).getId() == id){
				return i;
			}
		}
		return -1;
	}

	private void mergeLabels(Issue original, Issue edited, Issue latest, StringBuilder changeLog) {
		List<Label> originalLabels = original.getLabels();
		List<Label> editedLabels = edited.getLabels();
		HashMap<String, HashSet<Label>> changeSet = getChangesToList(originalLabels, editedLabels);
		List<Label> latestLabels = latest.getLabels();
		HashSet<Label> removed = changeSet.get(REMOVED_TAG);
		HashSet<Label> added = changeSet.get(ADDED_TAG);
		latestLabels.removeAll(removed);
		for(Label label: added){
			if(!latestLabels.contains(label)){
				latestLabels.add(label);
			}
		}
		logLabelChange(removed, added, changeLog);
		latest.setLabels(latestLabels);
	}

	/**
	 * Gets the changes made to the a list of items
	 * @return HashMap the a list of items removed from the original list
	 * 			and a list of items added to the original list
	 * */
	private <T> HashMap<String, HashSet<T>> getChangesToList(List<T> original, List<T> edited){
		HashMap<String, HashSet<T>> changeSet = new HashMap<String, HashSet<T>>();
		HashSet<T> removed = new HashSet<T>(original);
		HashSet<T> added = new HashSet<T>(edited);
		removed.removeAll(edited);
		added.removeAll(original);
		
		changeSet.put(REMOVED_TAG, removed);
		changeSet.put(ADDED_TAG, added);
		
		return changeSet;
	}
	
	private void logLabelChange(HashSet<Label> removed, HashSet<Label> added, StringBuilder changeLog){
		if(added.size() > 0){
			changeLog.append("Added labels: " + added.toString() + "\n");
		}
		if(removed.size() > 0){
			changeLog.append("Removed labels: " + removed.toString() + "\n");
		}
	}
	
	private void mergeMilestone(Issue original, Issue edited, Issue latest, StringBuilder changeLog) {
		Milestone originalMilestone = original.getMilestone();
		Milestone editedMilestone = edited.getMilestone();
		int originalMNumber = (originalMilestone != null) ? originalMilestone.getNumber() : 0;
		int editedMNumber = (editedMilestone != null) ? editedMilestone.getNumber() : 0;
		if (editedMNumber != originalMNumber) {
			// this check is for cleared milestone
			if (editedMilestone == null) {
				editedMilestone = new Milestone();
			}
			latest.setMilestone(editedMilestone);
			logMilestoneChange(editedMilestone, changeLog);
		}
	}

	private void logMilestoneChange(Milestone editedMilestone, StringBuilder changeLog){
		changeLog.append("Changed milestone to: "+ editedMilestone.getTitle() + "\n");
	}
	
	private void mergeState(Issue original, Issue edited, Issue latest, StringBuilder changeLog) {
		String originalState = original.getState();
		String editedState = edited.getState();
		if (!editedState.equals(originalState)) {
			latest.setState(editedState);
		}
	}

	private void mergeAssignee(Issue original, Issue edited, Issue latest, StringBuilder changeLog) {
		User originalAssignee = original.getAssignee();
		User editedAssignee = edited.getAssignee();
		String originalALogin = (originalAssignee != null) ? originalAssignee.getLogin() : "";
		String editedALogin = (editedAssignee != null) ? editedAssignee.getLogin() : "";
		if (!editedALogin.equals(originalALogin)) {
			// this check is for cleared assignee
			if (editedAssignee == null) {
				editedAssignee = new User();
			} 
			latest.setAssignee(editedAssignee);
			logAssigneeChange(editedAssignee, changeLog);
		}
	}
	
	private void logAssigneeChange(User assignee, StringBuilder changeLog){
		changeLog.append("Changed issue assignee to: "+ assignee.getLogin() + "\n");
	}

	private void mergeBody(Issue original, Issue edited, Issue latest) {
		String originalBody = original.getBody();
		String editedBody = edited.getBody();
		if (!editedBody.equals(originalBody)) {
			latest.setBody(editedBody);
		}
	}

	private void mergeTitle(Issue original, Issue edited, Issue latest) {
		String originalTitle = original.getTitle();
		String editedTitle = edited.getTitle();
		if (!editedTitle.equals(originalTitle)) {
			latest.setTitle(editedTitle);
		}
	}
	
	private boolean loadCollaborators() {
		collaborators.clear();	
		try {
			List<User> ghCollaborators = collabService.getCollaborators(repoId);
			for(User ghCollaborator : ghCollaborators) {
				collaborators.add(new TurboUser(ghCollaborator));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean loadIssues() {
		issues.clear();
		Map<String, String> filters = new HashMap<String, String>();
		filters.put(IssueService.FIELD_FILTER, STATE_ALL);
		filters.put(IssueService.FILTER_STATE, STATE_ALL);
		try {		
			List<Issue> ghIssues = issueService.getIssues(repoId, filters);
			for (Issue ghIssue : ghIssues) {
				issues.add(new TurboIssue(ghIssue));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean loadLabels(){
		labels.clear();
		try {
			List<Label> ghLabels = labelService.getLabels(repoId);
			for (Label ghLabel : ghLabels) {
				labels.add(new TurboLabel(ghLabel));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean loadMilestones(){
		milestones.clear();
		try {		
			List<Milestone> ghMilestones = milestoneService.getMilestones(repoId, STATE_ALL);
			for (Milestone ghMilestone : ghMilestones) {
				milestones.add(new TurboMilestone(ghMilestone));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}
