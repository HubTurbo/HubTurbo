package backend.github;

import backend.IssueMetadata;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import github.TurboIssueEvent;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Comment;
import util.HTLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DownloadMetadataTask extends GitHubRepoTask<Map<Integer, IssueMetadata>> {

	private static final Logger logger = HTLog.get(DownloadMetadataTask.class);

	private final String repoId;
	private final List<Integer> issueIds;

	public DownloadMetadataTask(TaskRunner taskRunner, Repo repo, String repoId,
	                            List<Integer> issueIds) {
		super(taskRunner, repo);
		this.repoId = repoId;
		this.issueIds = issueIds;
	}

	@Override
	public void run() {
		Map<Integer, IssueMetadata> result = new HashMap<>();

		issueIds.forEach(id -> {
			List<TurboIssueEvent> events = repo.getEvents(repoId, id);
			List<Comment> comments = repo.getComments(repoId, id);
			IssueMetadata metadata = new IssueMetadata(events, comments);
			result.put(id, metadata);
		});

		logger.info(HTLog.format(repoId, "Downloaded " + result.entrySet().stream()
			.map(entry -> "(" + entry.getValue().summarise() + ") for #" + entry.getKey())
			.collect(Collectors.joining(", "))));

		response.complete(result);
	}
}
