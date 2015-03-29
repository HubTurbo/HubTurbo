package service;

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

	public ModelUpdater(GitHubClientExtended client, Model model, UpdateSignature updateSignature) {
		this.model = model;
		this.issueUpdateService = new IssueUpdateService(client, updateSignature.issuesETag, updateSignature.lastCheckTime);
		this.collaboratorUpdateService = new CollaboratorUpdateService(client, updateSignature.collaboratorsETag);
		this.labelUpdateService = new LabelUpdateService(client, updateSignature.labelsETag);
		this.milestoneUpdateService = new MilestoneUpdateService(client, updateSignature.milestonesETag);
	}
	
	public void updateModel(String repoId) {
		logger.info("Updating model...");

		CountDownLatch latch = new CountDownLatch(4);
		model.disableModelChanges();

	    updateModelCollaborators(latch, repoId);
	   	updateModelLabels(latch, repoId);
	  	updateModelMilestones(latch, repoId);
	  	updateModelIssues(latch, repoId);

	  	model.enableModelChanges();

		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
	
	private void updateModelIssues(CountDownLatch latch, String repoId) {
		if (model.getRepoId().generateId().equals(repoId)) {
			List<Issue> updatedIssues = issueUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (updatedIssues.size() > 0) {
				model.updateCachedIssues(latch, updatedIssues, repoId);
			} else {
				logger.info("No issues to update");
				latch.countDown();
			}
		} else {
			logger.info("Repository has changed; not updating issues");
			latch.countDown();
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
		} else {
			logger.info("Repository has changed; not updating collaborators");
			latch.countDown();
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
		} else {
			logger.info("Repository has changed; not updating labels");
			latch.countDown();
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
		} else {
			logger.info("Repository has changed; not updating milestones");
			latch.countDown();
		}
	}

	public UpdateSignature getNewUpdateSignature() {
		return new UpdateSignature(issueUpdateService.getUpdatedETag(),
			labelUpdateService.getUpdatedETag(), milestoneUpdateService.getUpdatedETag(),
			collaboratorUpdateService.getUpdatedETag(), issueUpdateService.getUpdatedCheckTime());
	}
}
