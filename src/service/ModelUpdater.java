package service;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
	
	public boolean updateModel(String repoId) {
		logger.info("Updating model...");

		model.disableModelChanges();
		boolean result = true;

		try {
			updateModelCollaborators(repoId).get();
			updateModelLabels(repoId).get();
			updateModelMilestones(repoId).get();
			updateModelIssues(repoId).get();
		} catch (CancellationException e) {
			// Control jumping here means that one of the get methods
			// failed, i.e. one of the CompletableFutures was cancelled.
			// In that case we return false to stop the model update.
			result = false;
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getLocalizedMessage(), e);
		}

		model.enableModelChanges();
		return result;
	}

	private CompletableFuture<Void> updateModelIssues(String repoId) {
		CompletableFuture<Void> response = new CompletableFuture<>();
		if (model.getRepoId().generateId().equals(repoId)) {
			List<Issue> updatedIssues = issueUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (updatedIssues.size() > 0) {
				model.updateCachedIssues(response, updatedIssues, repoId);
			} else {
				logger.info("No issues to update");
				response.complete(null);
			}
		} else {
			logger.info("Repository has changed; not updating issues");
			response.cancel(true);
		}
		return response;
	}

	private CompletableFuture<Void> updateModelCollaborators(String repoId) {
		CompletableFuture<Void> response = new CompletableFuture<>();
		if (model.getRepoId().generateId().equals(repoId)) {
			List<User> collaborators = collaboratorUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (collaborators.size() > 0) {
				model.updateCachedCollaborators(response, collaborators, repoId);
			} else {
				logger.info("No collaborators to update");
				response.complete(null);
				HTStatusBar.addProgress(0.25);
			}
		} else {
			logger.info("Repository has changed; not updating collaborators");
			response.cancel(true);
		}
		return response;
	}

	private CompletableFuture<Void> updateModelLabels(String repoId) {
		CompletableFuture<Void> response = new CompletableFuture<>();
		if (model.getRepoId().generateId().equals(repoId)) {
			List<Label> labels = labelUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (labels.size() > 0) {
				model.updateCachedLabels(response, labels, repoId);
			} else {
				logger.info("No labels to update");
				response.complete(null);
				HTStatusBar.addProgress(0.25);
			}
		} else {
			logger.info("Repository has changed; not updating labels");
			response.cancel(true);
		}
		return response;
	}

	private CompletableFuture<Void> updateModelMilestones(String repoId) {
		CompletableFuture<Void> response = new CompletableFuture<>();
		if (model.getRepoId().generateId().equals(repoId)) {
			List<Milestone> milestones = milestoneUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (milestones.size() > 0) {
				model.updateCachedMilestones(response, milestones, repoId);
			} else {
				logger.info("No milestones to update");
				response.complete(null);
				HTStatusBar.addProgress(0.25);
			}
		} else {
			logger.info("Repository has changed; not updating milestones");
			response.cancel(true);
		}
		return response;
	}

	public UpdateSignature getNewUpdateSignature() {
		return new UpdateSignature(issueUpdateService.getUpdatedETag(),
			labelUpdateService.getUpdatedETag(), milestoneUpdateService.getUpdatedETag(),
			collaboratorUpdateService.getUpdatedETag(), issueUpdateService.getUpdatedCheckTime());
	}
}
