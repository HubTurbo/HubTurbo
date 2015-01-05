package service.updateservice;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

import service.GitHubClientExtended;
import ui.UIReference;
import ui.components.StatusBar;
import util.events.RefreshDoneEvent;
import model.Model;

public class ModelUpdater {
	private Model model;
	private IssueUpdateService issueUpdateService;
	private CollaboratorUpdateService collaboratorUpdateService;
	private LabelUpdateService labelUpdateService;
	private MilestoneUpdateService milestoneUpdateService;
	private long pollInterval = 60000; //time between polls in ms
	private Timer pollTimer;
	private int stopwatchInterval = 1000;
	private int timeRemaining = 60;
	private static final int SECS = 60;
	private Timer stopwatch;
	private Date lastUpdateTime = new Date();
	
	public ModelUpdater(GitHubClientExtended client, Model model, String issuesETag, String collabsETag, String labelsETag, String milestonesETag, String issueCheckTime){
		this.model = model;
		this.issueUpdateService = new IssueUpdateService(client, issuesETag, issueCheckTime);
		this.collaboratorUpdateService = new CollaboratorUpdateService(client, collabsETag);
		this.labelUpdateService = new LabelUpdateService(client, labelsETag);
		this.milestoneUpdateService = new MilestoneUpdateService(client, milestonesETag);
	}
	
	public Date getLastUpdateTime(){
		return lastUpdateTime;
	}
	
	private void updateModel(IRepositoryIdProvider repoId){
	    updateModelCollaborators(repoId);
	   	updateModelLabels(repoId);
	  	updateModelMilestones(repoId);
	  	updateModelIssues(repoId);
	  	lastUpdateTime = issueUpdateService.lastCheckTime;
	}
	
	private void updateModelIssues(IRepositoryIdProvider repoId){
		// here, we check if the repoId is the same as the one stored in model 
		// (as this method could have been triggered before project switching but executed after project switching)
		// when project switching occurs, the model will contain the new repoId so we stop i.e.
		// we don't get updated items or write them to file for the old repo. This prevents cache corruption.
		if (model.getRepoId().equals(repoId)) {
			List<Issue> updatedIssues = issueUpdateService.getUpdatedItems(repoId);	
			
			// if there are updates
			if (updatedIssues.size() > 0) {
				model.updateIssuesETag(issueUpdateService.getLastETag());
				model.updateIssueCheckTime(issueUpdateService.getLastIssueCheckTime());
				model.updateCachedIssues(updatedIssues, repoId.toString());
			}
		}
	}
	
	private void updateModelCollaborators(IRepositoryIdProvider repoId){
		if (model.getRepoId().equals(repoId)) {
			List<User> collaborators = collaboratorUpdateService.getUpdatedItems(repoId);
			if(collaborators.size() > 0){
				model.updateCollabsETag(collaboratorUpdateService.getLastETag());
				model.updateCachedCollaborators(collaborators, repoId.toString());
			}
		}
	}
	
	private void updateModelLabels(IRepositoryIdProvider repoId){
		if (model.getRepoId().equals(repoId)) {
			List<Label> labels = labelUpdateService.getUpdatedItems(repoId);
			if(labels.size() > 0){
				model.updateLabelsETag(labelUpdateService.getLastETag());
				model.updateCachedLabels(labels, repoId.toString());
			}
		}
	}
	
	private void updateModelMilestones(IRepositoryIdProvider repoId){
		if (model.getRepoId().equals(repoId)) {
			List<Milestone> milestones = milestoneUpdateService.getUpdatedItems(repoId);
			if(milestones.size() > 0){
				model.updateMilestonesETag(milestoneUpdateService.getLastETag());
				model.updateCachedMilestones(milestones, repoId.toString());
			}
		}
	}
	
	public void startModelUpdate(){
		if(pollTimer != null){
			stopModelUpdate();
		}
		pollTimer = new Timer();
		
		// get the current repo id from the model now so that the updates done will correspond with the current id in case of project switching
		final IRepositoryIdProvider repoId = model.getRepoId();
		TimerTask pollTask = new TimerTask(){
			@Override
			public void run() {
				updateModel(repoId);
				UIReference.getInstance().getUI().triggerEvent(new RefreshDoneEvent());
			}
		};
		pollTimer.scheduleAtFixedRate(pollTask, 0, pollInterval);
		
		stopwatch = new Timer();
		TimerTask countdown = new TimerTask() {
			@Override
			public void run() {
				StatusBar.displayMessage("Next refresh in " + getTime());
			}
		};
		stopwatch.scheduleAtFixedRate(countdown, 0, stopwatchInterval);
	}
	
	private int getTime() {
	    if (timeRemaining == 1) {
	        timeRemaining = SECS;
	    } else {
	    	--timeRemaining;
	    }
	    return timeRemaining;
	}
	
	public void stopModelUpdate(){
		if(pollTimer != null){
			pollTimer.cancel();
			pollTimer = null;
			stopwatch.cancel();
			timeRemaining = SECS;
			stopwatch = null;
		}
	}
}
