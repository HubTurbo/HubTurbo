package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.*;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.PullRequest;
import util.HTLog;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * This class represents an async task that downloads updates for a repository represented as a Model
 */
public class DownloadModelUpdatesTask extends GitHubRepoTask<GitHubModelUpdatesData> {

    private static final Logger logger = HTLog.get(DownloadModelUpdatesTask.class);

    private final Model model;

    public DownloadModelUpdatesTask(TaskRunner taskRunner, Repo repo, Model model) {
        super(taskRunner, repo);
        this.model = new Model(model);
    }

    @Override
    public void run() {
        DownloadIssuesUpdatesTask issuesTask = new DownloadIssuesUpdatesTask(taskRunner, repo, model);
        DownloadLabelsUpdatesTask labelsTask = new DownloadLabelsUpdatesTask(taskRunner, repo, model);
        DownloadMilestonesUpdatesTask milestonesTask = new DownloadMilestonesUpdatesTask(taskRunner, repo, model);
        DownloadUsersUpdatesTask usersTask = new DownloadUsersUpdatesTask(taskRunner, repo, model);
        DownloadPullRequestsUpdatesTask pullRequestsTask = new DownloadPullRequestsUpdatesTask(taskRunner, repo, model);

        taskRunner.execute(issuesTask);
        taskRunner.execute(labelsTask);
        taskRunner.execute(milestonesTask);
        taskRunner.execute(usersTask);
        taskRunner.execute(pullRequestsTask);

        try {
            Result<TurboIssue> issuesResult = issuesTask.response.get();
            Result<TurboLabel> labelsResult = labelsTask.response.get();
            Result<TurboMilestone> milestonesResult = milestonesTask.response.get();
            Result<TurboUser> usersResult = usersTask.response.get();
            List<PullRequest> pullRequestsResult = pullRequestsTask.response.get();

            GitHubModelUpdatesData updates = new GitHubModelUpdatesData(model,
                                                                        issuesResult, pullRequestsResult,
                                                                        labelsResult, milestonesResult, usersResult);
            logger.info(HTLog.format(model.getRepoId(), "Updates download completed"));
            response.complete(updates);
        } catch (InterruptedException | ExecutionException e) {
            HTLog.error(logger, e);
        }
    }
}
