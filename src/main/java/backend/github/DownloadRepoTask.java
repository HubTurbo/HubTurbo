package backend.github;

import backend.UpdateSignature;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.*;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.List;

public class DownloadRepoTask extends GitHubRepoTask<Model> {

	private static final Logger logger = HTLog.get(DownloadRepoTask.class);

	private final String repoId;

	public DownloadRepoTask(TaskRunner taskRunner, Repo repo, String repoId) {
		super(taskRunner, repo);
		this.repoId = repoId;
	}

	@Override
	public void run() {
		List<TurboIssue> issues = repo.getIssues(repoId);
		List<TurboLabel> labels = repo.getLabels(repoId);
		List<TurboMilestone> milestones = repo.getMilestones(repoId);
		List<TurboUser> users = repo.getCollaborators(repoId);
		Model result = new Model(repoId, issues, labels, milestones, users, UpdateSignature.empty);
		logger.info(HTLog.format(repoId, "Downloaded " + result.summarise()));
		response.complete(result);
	}
}
