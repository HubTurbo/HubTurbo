package service.updateservice;

import java.util.Date;
import java.util.List;

import model.Model;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;

import service.GitHubClientExtended;

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
	
	public void updateModel(String repoId){
	    updateModelCollaborators(repoId);
	   	updateModelLabels(repoId);
	  	updateModelMilestones(repoId);
	  	updateModelIssues(repoId);
	  	lastUpdateTime = issueUpdateService.lastCheckTime;
	  	model.triggerModelChangeEvent();
	}
	
	private void updateModelIssues(String repoId) {
		if (model.getRepoId().generateId().equals(repoId)) {
			List<Issue> updatedIssues = issueUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (updatedIssues.size() > 0) {
				model.updateIssuesETag(issueUpdateService.getLastETag());
				model.updateIssueCheckTime(issueUpdateService.getLastIssueCheckTime());
				model.updateCachedIssues(updatedIssues, repoId);
			}
		}
	}

	private void updateModelCollaborators(String repoId) {
		if (model.getRepoId().generateId().equals(repoId)) {
			List<User> collaborators = collaboratorUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (collaborators.size() > 0) {
				model.updateCollabsETag(collaboratorUpdateService.getLastETag());
				model.updateCachedCollaborators(collaborators, repoId);
			}
		}
	}

	private void updateModelLabels(String repoId) {
		if (model.getRepoId().generateId().equals(repoId)) {
			List<Label> labels = labelUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (labels.size() > 0) {
				model.updateLabelsETag(labelUpdateService.getLastETag());
				model.updateCachedLabels(labels, repoId);
			}
		}
	}

	private void updateModelMilestones(String repoId) {
		if (model.getRepoId().generateId().equals(repoId)) {
			List<Milestone> milestones = milestoneUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (milestones.size() > 0) {
				model.updateMilestonesETag(milestoneUpdateService.getLastETag());
				model.updateCachedMilestones(milestones, repoId);
			}
		}
	}
}
