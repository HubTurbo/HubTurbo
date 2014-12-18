package service.updateservice;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

import service.GitHubClientExtended;
import model.Model;

public class ModelUpdater {
	private Model model;
	private IssueUpdateService issueUpdateService;
	private CollaboratorUpdateService collaboratorUpdateService;
	private LabelUpdateService labelUpdateService;
	private MilestoneUpdateService milestoneUpdateService;
	private long pollInterval = 60000; //time between polls in ms
	private Timer pollTimer;
	private Date lastUpdateTime = new Date();
	
	public ModelUpdater(GitHubClientExtended client, Model model, String issuesETag, String collabsETag, String labelsETag, String milestonesETag){
		this.model = model;
		this.issueUpdateService = new IssueUpdateService(client, issuesETag);
		this.collaboratorUpdateService = new CollaboratorUpdateService(client, collabsETag);
		this.labelUpdateService = new LabelUpdateService(client, labelsETag);
		this.milestoneUpdateService = new MilestoneUpdateService(client, milestonesETag);
	}
	
	public Date getLastUpdateTime(){
		return lastUpdateTime;
	}
	
	private void updateModel(){
	    updateModelIssues();
	    updateModelCollaborators();
	   	updateModelLabels();
	  	updateModelMilestones();
	  	lastUpdateTime = issueUpdateService.lastCheckTime;
	}
	
	private void updateModelIssues(){
		List<Issue> updatedIssues = issueUpdateService.getUpdatedItems(model.getRepoId());
		model.updateIssuesETag(issueUpdateService.getLastETag());
		model.updateCachedIssues(updatedIssues);
	}
	
	private void updateModelCollaborators(){
		List<User> collaborators = collaboratorUpdateService.getUpdatedItems(model.getRepoId());
		model.updateCollabsETag(collaboratorUpdateService.getLastETag());
		if(collaborators.size() > 0){
			model.updateCachedCollaborators(collaborators);
		}
	}
	
	private void updateModelLabels(){
		List<Label> labels = labelUpdateService.getUpdatedItems(model.getRepoId());
		model.updateLabelsETag(labelUpdateService.getLastETag());
		if(labels.size() > 0){
			model.updateCachedLabels(labels);
		}
	}
	
	private void updateModelMilestones(){
		List<Milestone> milestones = milestoneUpdateService.getUpdatedItems(model.getRepoId());
		model.updateMilestonesETag(milestoneUpdateService.getLastETag());
		if(milestones.size() > 0){
			model.updateCachedMilestones(milestones);
		}
	}
	
	public void startModelUpdate(){
		if(pollTimer != null){
			stopModelUpdate();
		}
		pollTimer = new Timer();
		TimerTask pollTask = new TimerTask(){
			@Override
			public void run() {
				updateModel();
			}
		};
		pollTimer.scheduleAtFixedRate(pollTask, 0, pollInterval);
	}
	
	public void stopModelUpdate(){
		if(pollTimer != null){
			pollTimer.cancel();
			pollTimer = null;
		}
	}
}
