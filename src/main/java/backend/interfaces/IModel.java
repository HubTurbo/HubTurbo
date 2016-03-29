package backend.interfaces;

import backend.resource.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Only implemented by MultiModel.
 * This interface's purpose is to hide the public methods of MultiModel from
 * the UI.
 */
public interface IModel extends IBaseModel {
    String getDefaultRepo();

    void setDefaultRepo(String repoId);

    boolean isUserInRepo(String repoId, String userName);

    Optional<Model> getModelById(String repoId);

    Optional<TurboUser> getAssigneeOfIssue(TurboIssue issue);

    Optional<TurboUser> getAuthorOfIssue(TurboIssue issue);

    List<TurboLabel> getLabelsOfIssue(TurboIssue issue);

    List<TurboLabel> getLabelsOfIssue(TurboIssue issue, Predicate<TurboLabel> predicate);

    List<TurboUser> getUsersOfRepo(String repoId);

    Optional<TurboMilestone> getMilestoneOfIssue(TurboIssue issue);
}
