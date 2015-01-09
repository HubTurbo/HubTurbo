package service.updateservice;

import java.util.Date;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
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
	
	public void updateModel(IRepositoryIdProvider repoId){
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


}
