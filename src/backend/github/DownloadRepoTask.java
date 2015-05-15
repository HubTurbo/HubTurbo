package backend.github;

import backend.UpdateSignature;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.*;

import java.util.List;
import java.util.stream.Collectors;

public class DownloadRepoTask extends GitHubRepoTask<Model> {

	private static final Logger logger = LogManager.getLogger(DownloadRepoTask.class.getName());

	private final String repoId;

	public DownloadRepoTask(TaskRunner taskRunner, Repo<Issue, Label, Milestone, User> repo, String repoId) {
		super(taskRunner, repo);
		this.repoId = repoId;
	}

	@Override
	public void run() {
		List<TurboIssue> issues = repo.getIssues(repoId).stream()
			.map(TurboIssue::new)
			.collect(Collectors.toList());
		List<TurboLabel> labels = repo.getLabels(repoId).stream()
			.map(TurboLabel::new)
			.collect(Collectors.toList());
		List<TurboMilestone> milestones = repo.getMilestones(repoId).stream()
			.map(TurboMilestone::new)
			.collect(Collectors.toList());
		List<TurboUser> users = repo.getUsers(repoId).stream()
			.map(TurboUser::new)
			.collect(Collectors.toList());
		Model result = new Model(RepositoryId.createFromId(repoId), issues,
			labels, milestones, users, UpdateSignature.empty);
		logger.info("Downloaded " + result.summarise());
		response.complete(result);
	}
}
