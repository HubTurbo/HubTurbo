package backend.github;

import backend.IssueMetadata;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.TurboIssue;
import github.TurboIssueEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
    private final List<TurboIssue> issuesToUpdate;

    public DownloadMetadataTask(TaskRunner taskRunner, Repo repo, String repoId,
                                List<TurboIssue> issuesToUpdate) {
        super(taskRunner, repo);
        this.repoId = repoId;
        this.issuesToUpdate = issuesToUpdate;
    }

    @Override
    public void run() {
        Map<Integer, IssueMetadata> result = new HashMap<>();

        issuesToUpdate.forEach(issue -> {
            String currEventsETag = issue.getMetadata().getEventsETag();
            String currCommentsETag = issue.getMetadata().getCommentsETag();
            int id = issue.getId();

            ImmutablePair<List<TurboIssueEvent>, String> changes = repo.getUpdatedEvents(repoId, id, currEventsETag);

            List<TurboIssueEvent> events = changes.getLeft();
            String updatedEventsETag = changes.getRight();

            List<Comment> comments = repo.getAllComments(repoId, issue);

            IssueMetadata metadata = IssueMetadata.intermediate(events, comments, updatedEventsETag, currCommentsETag);
            result.put(id, metadata);
        });

        logger.info(HTLog.format(repoId, "Downloaded " + result.entrySet().stream()
                .map(entry -> "(" + entry.getValue().summarise() + ") " +
                        "for #" + entry.getKey())
                .collect(Collectors.joining(", "))));

        response.complete(result);
    }
}
