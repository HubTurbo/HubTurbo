package service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import model.Model;

import org.apache.commons.lang3.time.DateUtils;
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
	private Date lastUpdateTime = new Date();
	
	public ModelUpdater(GitHubClientExtended client, Model model, String issuesETag, String collabsETag, String
		labelsETag, String milestonesETag, Date issueCheckTime){
		this.model = model;
		this.issueUpdateService = new IssueUpdateService(client, issuesETag, issueCheckTime);
		this.collaboratorUpdateService = new CollaboratorUpdateService(client, collabsETag);
		this.labelUpdateService = new LabelUpdateService(client, labelsETag);
		this.milestoneUpdateService = new MilestoneUpdateService(client, milestonesETag);
	}
	
	public Date getLastUpdateTime(){
		return lastUpdateTime;
	}
	
	public void updateModel(CountDownLatch latch, String repoId) {
		logger.info("Updating model...");
		model.disableModelChanges();
		// TODO all these should return CompletableFuture<Integer>
		// with the number of resources updated
	    updateModelCollaborators(latch, repoId);
	   	updateModelLabels(latch, repoId);
	  	updateModelMilestones(latch, repoId);
	  	updateModelIssues(latch, repoId);
	  	lastUpdateTime = issueUpdateService.getUpdatedCheckTime();
	  	model.enableModelChanges();
	}
	
	private void updateModelIssues(CountDownLatch latch, String repoId) {
		// TODO turn this into an assertion, same for the rest
		if (model.getRepoId().generateId().equals(repoId)) {
			List<Issue> updatedIssues = issueUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (updatedIssues.size() > 0) {
				if (issueUpdateService.succeeded()) {
					model.updateIssuesETag(issueUpdateService.getUpdatedETag());
					model.updateIssueCheckTime(issueUpdateService.getUpdatedCheckTime());
					model.updateCachedIssues(latch, updatedIssues, repoId);
				} else {
					logger.info("Issues failed to update");
				}
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
				if (collaboratorUpdateService.succeeded()) {
					model.updateCollabsETag(collaboratorUpdateService.getUpdatedETag());
					model.updateCachedCollaborators(latch, collaborators, repoId);
				} else {
					logger.info("Collaborators failed to update");
				}
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
				if (labelUpdateService.succeeded()) {
					model.updateLabelsETag(labelUpdateService.getUpdatedETag());
					model.updateCachedLabels(latch, labels, repoId);
				} else {
					logger.info("Labels failed to update");
				}
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
				if (milestoneUpdateService.succeeded()) {
					model.updateMilestonesETag(milestoneUpdateService.getUpdatedETag());
					model.updateCachedMilestones(latch, milestones, repoId);
				} else {
					logger.info("Milestones failed to update");
				}
			} else {
				logger.info("No milestones to update");
				latch.countDown();
				HTStatusBar.addProgress(0.25);
			}
		}
	}
}
