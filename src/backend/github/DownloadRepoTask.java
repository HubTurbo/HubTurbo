package backend.github;

import backend.UpdateSignature;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.*;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.*;
import util.HTLog;

import java.util.List;
import java.util.stream.Collectors;

public class DownloadRepoTask extends GitHubRepoTask<Model> {

	private static final Logger logger = HTLog.get(DownloadRepoTask.class);

	private final String repoId;

	public DownloadRepoTask(TaskRunner taskRunner, Repo<Issue, Label, Milestone, User> repo, String repoId) {
		super(taskRunner, repo);
		this.repoId = repoId;
	}

	@Override
	public void run() {
		List<TurboIssue> issues = repo.getIssues(repoId).stream()
			.map(i -> new TurboIssue(repoId,  i))
			.collect(Collectors.toList());
		List<TurboLabel> labels = repo.getLabels(repoId).stream()
			.map(TurboLabel::new)
			.collect(Collectors.toList());
		List<TurboMilestone> milestones = repo.getMilestones(repoId).stream()
			.map(TurboMilestone::new)
			.collect(Collectors.toList());
		List<TurboUser> users = repo.getCollaborators(repoId).stream()
			.map(TurboUser::new)
			.collect(Collectors.toList());
		Model result = new Model(RepositoryId.createFromId(repoId), issues,
			labels, milestones, users, UpdateSignature.empty);
		logger.info(HTLog.format(repoId, "Downloaded " + result.summarise()));
		response.complete(result);
	}
}
