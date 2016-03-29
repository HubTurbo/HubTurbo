package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import backend.resource.TurboUser;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.List;

/**
 * This class represents an async task that downloads updates for users in a repository
 */
public class DownloadUsersUpdatesTask extends GitHubRepoTask<GitHubRepoTask.Result<TurboUser>> {

    private static final Logger logger = HTLog.get(DownloadUsersUpdatesTask.class);

    private final Model model;

    public DownloadUsersUpdatesTask(TaskRunner taskRunner, Repo repo, Model model) {
        super(taskRunner, repo);
        this.model = model;
    }

    @Override
    public void run() {
        ImmutablePair<List<TurboUser>, String> changes = repo.getUpdatedCollaborators(
                model.getRepoId(), model.getUpdateSignature().collaboratorsETag);

        List<TurboUser> changedUsers = changes.left;

        logger.info(HTLog.format(model.getRepoId(), "%s user(s)) changed%s",
                                 changedUsers.size(), changedUsers.isEmpty() ? "" : ": " + changedUsers));

        response.complete(new Result<>(changedUsers, changes.right));
    }
}
