package backend.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import backend.resource.*;

public interface IModel extends IBaseModel {
    public String getDefaultRepo();
    public void setDefaultRepo(String repoId);
    public Optional<Model> getModelById(String repoId);
    public Optional<TurboUser> getAssigneeOfIssue(TurboIssue issue);
    public List<TurboLabel> getLabelsOfIssue(TurboIssue issue);
    public List<TurboLabel> getLabelsOfIssue(TurboIssue issue, Predicate<TurboLabel> predicate);
    public Optional<TurboMilestone> getMilestoneOfIssue(TurboIssue issue);
}
