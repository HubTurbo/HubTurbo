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

	/**
	 * Updates the model given a source repository. May fail if the repository changes halfway through.
	 * This should not happen under normal circumstances and is a safeguard against concurrency issues.
	 * Getting an empty update does not constitute a failure.
	 *
	 * @param repoId the repository to get updates from
	 * @return true if the model update completed successfully
	 */
	public boolean updateModel(String repoId) {
		logger.info("Updating model...");

		model.disableModelChanges();
		boolean result = true;

		HTStatusBar.updateProgress(0.01);
		HTStatusBar.displayMessage("Updating collaborators...");

		try {
			log(updateModelCollaborators(repoId).get(), "collaborators", "labels");
			log(updateModelLabels(repoId).get(), "labels", "milestones");
			log(updateModelMilestones(repoId).get(), "milestones", "issues");
			log(updateModelIssues(repoId).get(), "issues", "comments");
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

	private static void log(int updated, String currentResourceName, String nextResourceName) {
		HTStatusBar.addProgressAndDisplayMessage(0.167, "Updating " + nextResourceName + "...");
		if (updated == 0) {
			logger.info("No " + currentResourceName + " to update");
		} else {
			logger.info(updated + " " + currentResourceName + " updated");
		}
	}

	/**
	 * Gets updates for issues. Returns a future which is completed on success and cancelled on failure.
	 * Failure means that the repository was changed halfway. It's a safeguard against concurrency issues.
	 * See {@link #updateModel(String)} for details.
	 *
	 * @param repoId the repository to get updates from
	 * @return a future which completes on success, and is cancelled upon failure
	 */
	private CompletableFuture<Integer> updateModelIssues(String repoId) {
		CompletableFuture<Integer> response = new CompletableFuture<>();
		if (model.getRepoId().generateId().equals(repoId)) {
			List<Issue> updatedIssues = issueUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (updatedIssues.size() > 0) {
				model.updateCachedIssues(response, updatedIssues, repoId);
			} else {
				response.complete(0);
			}
		} else {
			logger.info("Repository has changed; not updating issues");
			response.cancel(true);
		}
		return response;
	}

	/**
	 * See {@link #updateModelIssues(String)} for details.
	 */
	private CompletableFuture<Integer> updateModelCollaborators(String repoId) {
		CompletableFuture<Integer> response = new CompletableFuture<>();
		if (model.getRepoId().generateId().equals(repoId)) {
			List<User> collaborators = collaboratorUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (collaborators.size() > 0) {
				model.updateCachedCollaborators(response, collaborators, repoId);
			} else {
				response.complete(0);
			}
		} else {
			logger.info("Repository has changed; not updating collaborators");
			response.cancel(true);
		}
		return response;
	}

	/**
	 * See {@link #updateModelIssues(String)} for details.
	 */
	private CompletableFuture<Integer> updateModelLabels(String repoId) {
		CompletableFuture<Integer> response = new CompletableFuture<>();
		if (model.getRepoId().generateId().equals(repoId)) {
			List<Label> labels = labelUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (labels.size() > 0) {
				model.updateCachedLabels(response, labels, repoId);
			} else {
				response.complete(0);
			}
		} else {
			logger.info("Repository has changed; not updating labels");
			response.cancel(true);
		}
		return response;
	}

	/**
	 * See {@link #updateModelIssues(String)} for details.
	 */
	private CompletableFuture<Integer> updateModelMilestones(String repoId) {
		CompletableFuture<Integer> response = new CompletableFuture<>();
		if (model.getRepoId().generateId().equals(repoId)) {
			List<Milestone> milestones = milestoneUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
			if (milestones.size() > 0) {
				model.updateCachedMilestones(response, milestones, repoId);
			} else {
				response.complete(0);
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
