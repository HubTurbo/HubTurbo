package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import backend.resource.TurboIssue;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.Date;
import java.util.List;

/**
 * This class represents an async task that downloads updates for issues in a repository
 */
public class DownloadIssuesUpdatesTask extends GitHubRepoTask<GitHubRepoTask.Result<TurboIssue>> {

    private static final Logger logger = HTLog.get(DownloadIssuesUpdatesTask.class);

    private final Model model;

    public DownloadIssuesUpdatesTask(TaskRunner taskRunner, Repo repo, Model model) {
        super(taskRunner, repo);
        this.model = model;
    }

    @Override
    public void run() {
        ImmutableTriple<List<TurboIssue>, String, Date> changes = repo.getUpdatedIssues(model.getRepoId(),
                                                                                        model.getUpdateSignature()
                                                                                                .issuesETag, model
                                                                                                .getUpdateSignature()
                                                                                                .lastCheckTime);
        List<TurboIssue> updatedIssues = changes.left;
        logger.info(HTLog.format(model.getRepoId(), "%s issue(s)) changed%s",
                                 updatedIssues.size(), updatedIssues.isEmpty() ? "" : ": " + updatedIssues));
        response.complete(new Result<>(updatedIssues, changes.middle, changes.right));
    }
}
