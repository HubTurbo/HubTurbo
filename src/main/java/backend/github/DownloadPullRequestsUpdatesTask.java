package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.PullRequest;
import util.HTLog;

import java.util.List;

/**
 * This class represents an async task that downloads updates for pull requests in a repository
 */
public class DownloadPullRequestsUpdatesTask extends GitHubRepoTask<List<PullRequest>> {

    private static final Logger logger = HTLog.get(DownloadPullRequestsUpdatesTask.class);

    private final Model model;

    public DownloadPullRequestsUpdatesTask(TaskRunner taskRunner, Repo repo, Model model) {
        super(taskRunner, repo);
        this.model = model;
    }

    @Override
    public void run() {
        List<PullRequest> updatedPullRequests = repo.getUpdatedPullRequests(
                model.getRepoId(), model.getUpdateSignature().lastCheckTime);
        logger.info(HTLog.format(model.getRepoId(), "%s pr(s)) changed%s",
                updatedPullRequests.size(), updatedPullRequests.isEmpty() ? "" : ": " + updatedPullRequests));
        response.complete(updatedPullRequests);
    }
}
