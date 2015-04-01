package command;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.RequestException;

import service.ServiceManager;
import util.CollectionUtilities;
import util.DialogMessage;

public class TurboIssueEdit extends TurboIssueCommand{
	protected static final String TITLE_FIELD = "title";
	protected static final String DESCRIPTION_FIELD = "desc";
	protected static final String LABEL_FIELD = "label";
	protected static final String MILESTONE_FIELD = "milestone";
	protected static final String ASSIGNEE_FIELD = "assignee";
	protected static final String PARENT_FIELD = "parent";
	
	private TurboIssue editedIssue;
	
	public TurboIssueEdit(Model model, TurboIssue originalIssue, TurboIssue editedIssue){
		super(model, originalIssue);
		this.editedIssue = editedIssue;
	}
	
	public TurboIssue getEditedIssue(){
		return editedIssue;
	}
	
	@Override
	public boolean performExecuteAction() {
		isSuccessful = updateIssue(issue, editedIssue);
		return isSuccessful;
	}
	
	@Override
	public boolean performUndoAction() {
		return true;
	}
		
	private void updateIssueInGithub(Issue sent, String dateModified) throws IOException{
		ServiceManager.getInstance().editIssue(sent, dateModified);
	}
	
	private boolean updateIssue(TurboIssue originalIssue, TurboIssue editedIssue){
		int issueId = editedIssue.getId();
		HashMap<String, Object> issueQuery;
		try {
			issueQuery = ServiceManager.getInstance().getIssueData(issueId);
			
			String dateModified = ServiceManager.getInstance().getDateFromIssueData(issueQuery);
			TurboIssue latestIssue = new TurboIssue(ServiceManager.getInstance().getIssueFromIssueData(issueQuery), model.get());
			
			boolean descUpdated = mergeIssues(originalIssue, editedIssue, latestIssue);
			Issue latest = latestIssue.toGhResource();
			updateIssueInGithub(latest, dateModified);
						
			if(!descUpdated){
				DialogMessage.showWarningDialog("Issue description not updated", "The issue description has been concurrently modified. "
						+ "Please reload and enter your descripton again.");
			}
			
			Platform.runLater(() -> {
				//Must be run on application thread since this triggers ui updates.
				model.get().updateCachedIssue(latestIssue);
			});
			return true;
		} catch (IOException e) {
			if(e instanceof SocketTimeoutException | e instanceof UnknownHostException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("Internet Connection Timeout", 
							"Timeout while editing issue in GitHub, please check your internet connection.");
				});
				logger.info("Could not edit issue: " + e.getLocalizedMessage());
			}else if(e instanceof RequestException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("No repository permissions", 
							"Cannot edit issue.");
				});
				logger.info("Could not edit issue: " + e.getLocalizedMessage());
			}else{
				logger.error(e.getLocalizedMessage(), e);
			}
			return false;
		}
	}
	
	/**
	 * Modifies @param latest to contain the merged changes of @param edited and @param latest wrt @param edited
	 * Stores change log in @param changeLog
	 * @return true if issue description has been successfully merged, false otherwise
	 * */
	private boolean mergeIssues(TurboIssue original, TurboIssue edited, TurboIssue latest){
		mergeTitle(original, edited, latest);
		boolean fullMerge = mergeDescription(original, edited, latest);
		mergeIssueParent(original, edited, latest);
		mergeLabels(original, edited, latest);
		mergeAssignee(original, edited, latest);
		mergeMilestone(original, edited, latest);
		mergeOpen(original, edited, latest);
		return fullMerge;
	}
	
	private void mergeLabels(TurboIssue original, TurboIssue edited, TurboIssue latest) {
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
//		changeLogs.put(LABEL_FIELD, IssueChangeLogger.getLabelsChangeLog(model.get(), originalLabels, editedLabels));
		latest.setLabels(latestLabels);
	}
	
	private void mergeMilestone(TurboIssue original, TurboIssue edited, TurboIssue latest) {
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
		}
	}
	
	private void mergeOpen(TurboIssue original, TurboIssue edited, TurboIssue latest) {
		Boolean originalState = original.isOpen();
		Boolean editedState = edited.isOpen();
		if (!editedState.equals(originalState)) {
			latest.setOpen(editedState);
		}
	}

	private void mergeAssignee(TurboIssue original, TurboIssue edited, TurboIssue latest) {
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
		}
	}

	/**
	 * Merges changes to description only if the description in the latest version has not been updated. 
	 * Returns false if description was not merged because the issue's description has been modified in @param latest
	 * */
	private boolean mergeDescription(TurboIssue original, TurboIssue edited, TurboIssue latest) {
		String originalDesc = original.getDescription();
		String editedDesc = edited.getDescription();
		String latestDesc = latest.getDescription();
		if (!editedDesc.equals(originalDesc)) {
			if(!latestDesc.equals(originalDesc)){
				return false;
			}
			latest.setDescription(editedDesc);
		}
		return true;
	}
	
	private void mergeIssueParent(TurboIssue original, TurboIssue edited, TurboIssue latest){
		Integer originalParent = original.getParentIssue();
		Integer editedParent = edited.getParentIssue();
		
		if(originalParent != editedParent){
			latest.setParentIssue(editedParent);
			processInheritedLabels(originalParent, editedParent, edited);
		}
	}

	private void mergeTitle(TurboIssue original, TurboIssue edited, TurboIssue latest) {
		String originalTitle = original.getTitle();
		String editedTitle = edited.getTitle();
		if (!editedTitle.equals(originalTitle)) {
			latest.setTitle(editedTitle);
		}
	}
}
