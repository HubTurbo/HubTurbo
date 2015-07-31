package backend.github;

import backend.IssueMetadata;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import github.TurboIssueEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Comment;
import util.HTLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DownloadMetadataTask extends GitHubRepoTask<Map<Integer, IssueMetadata>> {

    private static final Logger logger = HTLog.get(DownloadMetadataTask.class);

    private final String repoId;
    private final Map<Integer, String> issueIdETags;

    public DownloadMetadataTask(TaskRunner taskRunner, Repo repo, String repoId,
                                Map<Integer, String> issueIdETags) {
        super(taskRunner, repo);
        this.repoId = repoId;
        this.issueIdETags = issueIdETags;
    }

    @Override
    public void run() {
        Map<Integer, IssueMetadata> result = new HashMap<>();

        issueIdETags.forEach((id, currentETag) -> {
            ImmutablePair<List<TurboIssueEvent>, String> changes = repo.getUpdatedEvents(repoId, id, currentETag);

            List<TurboIssueEvent> events = changes.getLeft();
            String updatedETag = changes.getRight();

            List<Comment> comments = new ArrayList<>();
            if (!updatedETag.equals(currentETag)) comments = repo.getComments(repoId, id);

            IssueMetadata metadata = new IssueMetadata(events, comments, updatedETag);
            result.put(id, metadata);
        });

        logger.info(HTLog.format(repoId, "Downloaded " + result.entrySet().stream()
            .map(entry -> "(" + entry.getValue().summarise() + ") for #" + entry.getKey())
            .collect(Collectors.joining(", "))));

        response.complete(result);
    }
}
