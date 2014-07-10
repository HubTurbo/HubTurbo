package util;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

import service.GitHubClientExtended;
import service.ServiceManager;
import service.updateservice.CollaboratorUpdateService;
import service.updateservice.IssueUpdateService;
import service.updateservice.LabelUpdateService;
import service.updateservice.MilestoneUpdateService;
import model.Model;

public class ModelUpdater {
	private Model model;
	private IssueUpdateService issueUpdateService;
	private CollaboratorUpdateService collaboratorUpdateService;
	private LabelUpdateService labelUpdateService;
	private MilestoneUpdateService milestoneUpdateService;
	private long pollInterval = 60000; //time between polls in ms
	private Timer pollTimer;
	
	public ModelUpdater(GitHubClientExtended client, Model model){
		this.model = model;
		this.issueUpdateService = new IssueUpdateService(client);
		this.collaboratorUpdateService = new CollaboratorUpdateService(client);
		this.labelUpdateService = new LabelUpdateService(client);
		this.milestoneUpdateService = new MilestoneUpdateService(client);
	}
	
	private void updateModel(){
	    updateModelIssues();
	    updateModelCollaborators();
	   	updateModelLabels();
	  	updateModelMilestones();
	}
	
	private void updateModelIssues(){
		List<Issue> updatedIssues = issueUpdateService.getUpdatedItems(ServiceManager.getInstance().getRepoId());
		model.updateCachedIssues(updatedIssues);
	}
	
	private void updateModelCollaborators(){
		List<User> collaborators = collaboratorUpdateService.getUpdatedItems(ServiceManager.getInstance().getRepoId());
		if(collaborators.size() > 0){
			model.updateCachedCollaborators(collaborators);
		}
	}
	
	private void updateModelLabels(){
		List<Label> labels = labelUpdateService.getUpdatedItems(ServiceManager.getInstance().getRepoId());
		if(labels.size() > 0){
			model.updateCachedLabels(labels);
		}
	}
	
	private void updateModelMilestones(){
		List<Milestone> milestones = milestoneUpdateService.getUpdatedItems(ServiceManager.getInstance().getRepoId());
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
