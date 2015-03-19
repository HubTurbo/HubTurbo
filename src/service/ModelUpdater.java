package service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import model.Model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;

import service.updateservice.CollaboratorUpdateService;
import service.updateservice.IssueUpdateService;
import service.updateservice.LabelUpdateService;
import service.updateservice.MilestoneUpdateService;
import ui.components.HTStatusBar;

public class ModelUpdater {
	
	private static final Logger logger = LogManager.getLogger(ModelUpdater.class.getName());

	private Model model;
	private IssueUpdateService issueUpdateService;
	private CollaboratorUpdateService collaboratorUpdateService;
	private LabelUpdateService labelUpdateService;
	private MilestoneUpdateService milestoneUpdateService;

	public ModelUpdater(GitHubClientExtended client, Model model, String issuesETag, String labelsETag,
            String milestonesETag, String collabsETag, Date issueCheckTime){
		this.model = model;
		this.issueUpdateService = new IssueUpdateService(client, issuesETag, issueCheckTime);
		this.collaboratorUpdateService = new CollaboratorUpdateService(client, collabsETag);
		this.labelUpdateService = new LabelUpdateService(client, labelsETag);
		this.milestoneUpdateService = new MilestoneUpdateService(client, milestonesETag);
	}
	
	public void updateModel(CountDownLatch latch, String repoId) {
		logger.info("Updating model...");
		model.disableModelChanges();
		// TODO all these should return CompletableFuture<Integer> with the number of resources updated
	    updateModelCollaborators(latch, repoId);
	   	updateModelLabels(latch, repoId);
	  	updateModelMilestones(latch, repoId);
	  	updateModelIssues(latch, repoId);
	  	model.enableModelChanges();
	}
	
	private void updateModelIssues(CountDownLatch latch, String repoId) {
		// TODO turn this into an assertion, same for the rest
		if (model.getRepoId().generateId().equals(repoId)) {
			List<Issue> updatedIssues = issueUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (updatedIssues.size() > 0) {
				model.updateCachedIssues(latch, updatedIssues, repoId);
			} else {
				logger.info("No issues to update");
				latch.countDown();
			}
		}
	}

	private void updateModelCollaborators(CountDownLatch latch, String repoId) {
		if (model.getRepoId().generateId().equals(repoId)) {
			List<User> collaborators = collaboratorUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (collaborators.size() > 0) {
				model.updateCachedCollaborators(latch, collaborators, repoId);
			} else {
				logger.info("No collaborators to update");
				latch.countDown();
				HTStatusBar.addProgress(0.25);
			}
		}
	}

	private void updateModelLabels(CountDownLatch latch, String repoId) {
		if (model.getRepoId().generateId().equals(repoId)) {
			List<Label> labels = labelUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (labels.size() > 0) {
				model.updateCachedLabels(latch, labels, repoId);
			} else {
				logger.info("No labels to update");
				latch.countDown();
				HTStatusBar.addProgress(0.25);
			}
		}
	}

	private void updateModelMilestones(CountDownLatch latch, String repoId) {
		if (model.getRepoId().generateId().equals(repoId)) {
			List<Milestone> milestones = milestoneUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (milestones.size() > 0) {
				model.updateCachedMilestones(latch, milestones, repoId);
			} else {
				logger.info("No milestones to update");
				latch.countDown();
				HTStatusBar.addProgress(0.25);
			}
		}
	}

	public Date getLastUpdateTime() {
		return issueUpdateService.getUpdatedCheckTime();
	}

	public String getUpdatedIssueETag() {
		return issueUpdateService.getUpdatedETag();
	}

	public String getUpdatedLabelETag() {
		return labelUpdateService.getUpdatedETag();
	}

	public String getUpdatedMilestoneETag() {
		return milestoneUpdateService.getUpdatedETag();
	}

	public String getUpdatedCollaboratorETag() {
		return collaboratorUpdateService.getUpdatedETag();
	}

}
