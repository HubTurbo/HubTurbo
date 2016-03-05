package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import backend.resource.TurboMilestone;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.List;

/**
 * This class represents an async task that downloads updates for milestones in a repository
 */
public class DownloadMilestonesUpdatesTask extends GitHubRepoTask<GitHubRepoTask.Result<TurboMilestone>> {

    private static final Logger logger = HTLog.get(DownloadMilestonesUpdatesTask.class);

    private final Model model;

    public DownloadMilestonesUpdatesTask(TaskRunner taskRunner, Repo repo, Model model) {
        super(taskRunner, repo);
        this.model = model;
    }

    @Override
    public void run() {
        ImmutablePair<List<TurboMilestone>, String> changes = repo.getUpdatedMilestones(model.getRepoId(),
            model.getUpdateSignature().milestonesETag);

        List<TurboMilestone> changedMilestones = changes.left;

        logger.info(HTLog.format(model.getRepoId(), "%s milestone(s)) changedMilestones%s",
            changedMilestones.size(), changedMilestones.isEmpty() ? "" : ": " + changedMilestones));

        response.complete(new Result<>(changedMilestones, changes.right));
    }
}
