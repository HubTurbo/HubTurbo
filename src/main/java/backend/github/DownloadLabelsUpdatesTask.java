package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import backend.resource.TurboLabel;
import backend.tupleresults.ListStringResult;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.List;

/**
 * This class represents an async task that downloads updates for labels in a repository
 */
public class DownloadLabelsUpdatesTask extends GitHubRepoTask<GitHubRepoTask.Result<TurboLabel>> {

    private static final Logger logger = HTLog.get(DownloadLabelsUpdatesTask.class);

    private final Model model;

    public DownloadLabelsUpdatesTask(TaskRunner taskRunner, Repo repo, Model model) {
        super(taskRunner, repo);
        this.model = model;
    }

    @Override
    public void run() {
        ListStringResult changes = repo.getUpdatedLabels(model.getRepoId(),
            model.getUpdateSignature().labelsETag);

        List<TurboLabel> changedLabels = changes.getList();

        logger.info(HTLog.format(model.getRepoId(), "%s label(s)) changed%s",
            changedLabels.size(), changedLabels.isEmpty() ? "" : ": " + changedLabels));

        response.complete(new Result<>(changedLabels, changes.getString()));
    }
}
