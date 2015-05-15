package backend.github;

import backend.resource.Model;
import backend.resource.TurboIssue;
import backend.UpdateSignature;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DownloadRepoTask extends GitHubRepoTask<Model> {

	private static final Logger logger = LogManager.getLogger(DownloadRepoTask.class.getName());

	private final String repoId;

	public DownloadRepoTask(TaskRunner taskRunner, Repo<Issue> repo, String repoId) {
		super(taskRunner, repo);
		this.repoId = repoId;
	}

	@Override
	public void run() {
		List<TurboIssue> issues = repo.getIssues(repoId).stream()
			.map(TurboIssue::new)
			.collect(Collectors.toList());
		Model result = new Model(RepositoryId.createFromId(repoId), issues,
			new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), UpdateSignature.empty);
		logger.info("Downloaded " + result.summarise());
		response.complete(result);
	}
}
