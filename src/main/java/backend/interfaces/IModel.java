package backend.interfaces;

import backend.resource.*;

import java.util.List;
import java.util.Optional;

public interface IModel extends IBaseModel {
	public String getDefaultRepo();
	public void setDefaultRepo(String repoId);
	public Optional<Model> getModelById(String repoId);
	public Optional<TurboUser> getAssigneeOfIssue(TurboIssue issue);
	public List<TurboLabel> getLabelsOfIssue(TurboIssue issue);
	public Optional<TurboMilestone> getMilestoneOfIssue(TurboIssue issue);
}
