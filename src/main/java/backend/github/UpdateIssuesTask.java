package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import backend.resource.TurboIssue;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.PullRequest;
import util.HTLog;

import java.util.Date;
import java.util.List;

public class UpdateIssuesTask extends GitHubRepoTask<GitHubRepoTask.Result<TurboIssue>> {

    private static final Logger logger = HTLog.get(UpdateIssuesTask.class);

    private final Model model;

    public UpdateIssuesTask(TaskRunner taskRunner, Repo repo, Model model) {
        super(taskRunner, repo);
        this.model = model;
    }

    @Override
    public void run() {
        ImmutableTriple<List<TurboIssue>, String, Date> changes = repo.getUpdatedIssues(model.getRepoId(),
            model.getUpdateSignature().issuesETag, model.getUpdateSignature().lastCheckTime);
        List<PullRequest> updatedPullRequests = repo.getUpdatedPullRequests(
                model.getRepoId(), model.getUpdateSignature().lastCheckTime);

        List<TurboIssue> existing = model.getIssues();
        List<TurboIssue> updatedIssues = changes.left;

        logger.info(HTLog.format(model.getRepoId(), "%s issue(s)) changed%s",
                updatedIssues.size(), updatedIssues.isEmpty() ? "" : ": " + updatedIssues));
        logger.info(HTLog.format(model.getRepoId(), "%s pr(s)) changed%s",
                updatedPullRequests.size(), updatedPullRequests.isEmpty() ? "" : ": " + updatedPullRequests));

        List<TurboIssue> updated = updatedIssues.isEmpty()
            ? existing
            : TurboIssue.reconcile(existing, updatedIssues);
        updated = TurboIssue.combineWithPullRequests(updated, updatedPullRequests);

        response.complete(new Result<>(updated, changes.middle, changes.right));
    }
}
