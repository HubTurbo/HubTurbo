package command;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.egit.github.core.Issue;

import javafx.collections.ObservableList;
import service.ServiceManager;
import util.CollectionUtilities;
import util.DialogMessage;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

public class TurboIssueEdit extends TurboIssueCommand{
	private TurboIssue editedIssue;
	
	public TurboIssueEdit(Model model, TurboIssue originalIssue, TurboIssue editedIssue){
		super(model, originalIssue);
		this.editedIssue = editedIssue;
	}
	
	
	@Override
	public boolean execute() {
		isSuccessful = updateIssue(issue, editedIssue);
		return isSuccessful;
	}
	
	@Override
	public boolean undo() {
		return true;
	}
	
	private boolean updateIssue(TurboIssue originalIssue, TurboIssue editedIssue){
		int issueId = editedIssue.getId();
		StringBuilder changeLog = new StringBuilder();
		HashMap<String, Object> issueQuery;
		try {
			issueQuery = ServiceManager.getInstance().getIssueData(issueId);
			
			String dateModified = ServiceManager.getInstance().getDateFromIssueData(issueQuery);
			TurboIssue latestIssue = new TurboIssue(ServiceManager.getInstance().getIssueFromIssueData(issueQuery), model.get());
			
			boolean descUpdated = mergeIssues(originalIssue, editedIssue, latestIssue, changeLog);
			Issue latest = latestIssue.toGhResource();
			ServiceManager.getInstance().editIssue(latest, dateModified);
			
			logChanges(changeLog);
			
			if(!descUpdated){
				DialogMessage.showWarningDialog("Issue description not updated", "The issue description has been concurrently modified. "
						+ "Please refresh and enter your descripton again.");
			}
			
			model.get().updateCachedIssue(latestIssue);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
	}
	
	private void logChanges(StringBuilder changeLog){
		if(changeLog.length() > 0){
			lastOperationExecuted = changeLog.toString();
			ServiceManager.getInstance().logIssueChanges(issue.getId(), changeLog.toString());
		}
	}
	
	/**
	 * Modifies @param latest to contain the merged changes of @param edited and @param latest wrt @param edited
	 * Stores change log in @param changeLog
	 * @return true if issue description has been successfully merged, false otherwise
	 * */
	private boolean mergeIssues(TurboIssue original, TurboIssue edited, TurboIssue latest, StringBuilder changeLog){
		mergeTitle(original, edited, latest, changeLog);
		boolean fullMerge = mergeDescription(original, edited, latest, changeLog);
		mergeIssueParent(original, edited, latest, changeLog);
		mergeLabels(original, edited, latest, changeLog);
		mergeAssignee(original, edited, latest, changeLog);
		mergeMilestone(original, edited, latest, changeLog);
		mergeOpen(original, edited, latest);
		return fullMerge;
	}
	
	private void mergeLabels(TurboIssue original, TurboIssue edited, TurboIssue latest, StringBuilder changeLog) {
		ObservableList<TurboLabel> originalLabels = original.getLabels();
		ObservableList<TurboLabel> editedLabels = edited.getLabels();
		HashMap<String, HashSet<TurboLabel>> changeSet = CollectionUtilities.getChangesToList(originalLabels, editedLabels);
		ObservableList<TurboLabel> latestLabels = latest.getLabels();
		HashSet<TurboLabel> removed = changeSet.get(CollectionUtilities.REMOVED_TAG);
		HashSet<TurboLabel> added = changeSet.get(CollectionUtilities.ADDED_TAG);
		
		latestLabels.removeAll(removed);
		for(TurboLabel label: added){
			if(!latestLabels.contains(label)){
				latestLabels.add(label);
			}
		}
		changeLog.append(IssueChangeLogger.getLabelsChangeLog(model.get(), originalLabels, editedLabels));
		latest.setLabels(latestLabels);
	}
	
	private void mergeMilestone(TurboIssue original, TurboIssue edited, TurboIssue latest, StringBuilder changeLog) {
		TurboMilestone originalMilestone = original.getMilestone();
		TurboMilestone editedMilestone = edited.getMilestone();
		int originalMNumber = (originalMilestone != null) ? originalMilestone.getNumber() : 0;
		int editedMNumber = (editedMilestone != null) ? editedMilestone.getNumber() : 0;
		if (editedMNumber != originalMNumber) {
			// this check is for cleared milestone
			if (editedMilestone == null) {
				editedMilestone = new TurboMilestone();
			}
			if (originalMilestone == null) {
				originalMilestone = new TurboMilestone();
			}
			latest.setMilestone(editedMilestone);
			changeLog.append(IssueChangeLogger.getMilestoneChangeLog(originalMilestone, editedMilestone));
		}
	}
	
	private void mergeOpen(TurboIssue original, TurboIssue edited, TurboIssue latest) {
		Boolean originalState = original.getOpen();
		Boolean editedState = edited.getOpen();
		if (!editedState.equals(originalState)) {
			latest.setOpen(editedState);
		}
	}

	private void mergeAssignee(TurboIssue original, TurboIssue edited, TurboIssue latest, StringBuilder changeLog) {
		TurboUser originalAssignee = original.getAssignee();
		TurboUser editedAssignee = edited.getAssignee();
		// this check is for cleared assignee
		if(originalAssignee == null){
			originalAssignee = new TurboUser();
		}
		if (editedAssignee == null) {
			editedAssignee = new TurboUser();
		} 
		if (!originalAssignee.equals(editedAssignee)) {
			latest.setAssignee(editedAssignee);
			changeLog.append(IssueChangeLogger.getAssigneeChangeLog(originalAssignee, editedAssignee));
		}
	}

	/**
	 * Merges changes to description only if the description in the latest version has not been updated. 
	 * Returns false if description was not merged because the issue's description has been modified in @param latest
	 * */
	private boolean mergeDescription(TurboIssue original, TurboIssue edited, TurboIssue latest, StringBuilder changeLog) {
		String originalDesc = original.getDescription();
		String editedDesc = edited.getDescription();
		String latestDesc = latest.getDescription();
		if (!editedDesc.equals(originalDesc)) {
			if(!latestDesc.equals(originalDesc)){
				return false;
			}
			latest.setDescription(editedDesc);
			changeLog.append(IssueChangeLogger.getDescriptionChangeLog(originalDesc, editedDesc));
		}
		return true;
	}
	
	private void mergeIssueParent(TurboIssue original, TurboIssue edited, TurboIssue latest, StringBuilder changeLog){
		Integer originalParent = original.getParentIssue();
		Integer editedParent = edited.getParentIssue();
		
		if(originalParent != editedParent){
			latest.setParentIssue(editedParent);
			processInheritedLabels(originalParent, editedParent, edited);
			changeLog.append(IssueChangeLogger.getParentChangeLog(originalParent, editedParent));
		}
	}

	private void mergeTitle(TurboIssue original, TurboIssue edited, TurboIssue latest, StringBuilder changeLog) {
		String originalTitle = original.getTitle();
		String editedTitle = edited.getTitle();
		if (!editedTitle.equals(originalTitle)) {
			latest.setTitle(editedTitle);
			changeLog.append(IssueChangeLogger.getTitleChangeLog(originalTitle, editedTitle));
		}
	}
}
