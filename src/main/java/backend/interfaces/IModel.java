package backend.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import backend.resource.*;

/**
 * Only implemented by MultiModel.
 * This interface's purpose is to hide the public methods of MultiModel from
 * the UI.
  */
public interface IModel extends IBaseModel {
    String getDefaultRepo();
    void setDefaultRepo(String repoId);
    Optional<Model> getModelById(String repoId);
    Optional<TurboUser> getAssigneeOfIssue(TurboIssue issue);
    List<TurboLabel> getLabelsOfIssue(TurboIssue issue);
    List<TurboLabel> getLabelsOfIssue(TurboIssue issue, Predicate<TurboLabel> predicate);
    Optional<TurboMilestone> getMilestoneOfIssue(TurboIssue issue);
}
