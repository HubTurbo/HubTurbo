package backend.control.operations;

import backend.UpdateSignature;
import backend.github.GitHubRepoUpdatesData;
import backend.resource.*;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.PullRequest;
import util.HTLog;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This class is a mutually exclusive operation that update a locally stored repository
 * represented by a Model with data downloaded from server
 */
public class UpdateLocalModelOp implements RepoOp<Optional<Model>> {
    private final MultiModel models;
    private Model oldModel;
    private final CompletableFuture<Optional<Model>> result;
    private final GitHubRepoUpdatesData updates;

    private static final Logger logger = HTLog.get(UpdateLocalModelOp.class);

    public UpdateLocalModelOp(MultiModel models, GitHubRepoUpdatesData updates,
                              CompletableFuture<Optional<Model>> result) {
        this.models = models;
        this.updates = updates;
        this.result = result;
    }

    @Override
    public String repoId() {
        return updates.getRepoId();
    }

    @Override
    public CompletableFuture<Optional<Model>> perform() {
        Optional<Model> oldModelOptional = models.getModelById(updates.getRepoId());
        if (!oldModelOptional.isPresent()) {
            logger.info(updates.getRepoId(), "Updates discarded, local model is not found");
            result.complete(Optional.empty());
            return result;
        }

        this.oldModel = oldModelOptional.get();
        UpdateSignature newSignature =
                new UpdateSignature(updates.getIssues().eTag, updates.getLabels().eTag,
                                    updates.getMilestones().eTag, updates.getUsers().eTag,
                                    updates.getIssues().lastCheckTime);
        Model updatedModel = new Model(updates.getRepoId(), getUpdateIssues(), getUpdatedLabels(),
                                       getUpdatedMilestones(), getUpdatedUsers(), newSignature);

        logger.info(HTLog.format(updatedModel.getRepoId(), "Updated model with " + updatedModel.summarise()));
        models.replace(updatedModel);
        result.complete(Optional.of(updatedModel));
        return result;
    }

    private List<TurboIssue> getUpdateIssues() {
        List<TurboIssue> existing = oldModel.getIssues();
        List<TurboIssue> updatedIssues = updates.getIssues().items;
        List<PullRequest> updatesPullRequests = updates.getPullRequests();

        List<TurboIssue> updated = updatedIssues.isEmpty() ? existing : TurboIssue.reconcile(existing, updatedIssues);
        return TurboIssue.combineWithPullRequests(updated, updatesPullRequests);
    }

    private List<TurboLabel> getUpdatedLabels() {
        return updates.getLabels().items.isEmpty() ? oldModel.getLabels() : updates.getLabels().items;
    }

    private List<TurboMilestone> getUpdatedMilestones() {
        return updates.getMilestones().items.isEmpty() ? oldModel.getMilestones() : updates.getMilestones().items;
    }

    private List<TurboUser> getUpdatedUsers() {
        return updates.getUsers().items.isEmpty() ? oldModel.getUsers() : updates.getUsers().items;
    }
}
