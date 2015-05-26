package backend.interfaces;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;

import java.util.List;

public interface IBaseModel {
	public List<TurboIssue> getIssues();
	public List<TurboLabel> getLabels();
	public List<TurboMilestone> getMilestones();
	public List<TurboUser> getUsers();

	public default String summarise() {
		return String.format("%d issue(s), %d label(s), %d milestone(s), %d user(s)",
			getIssues().size(),
			getLabels().size(),
			getMilestones().size(),
			getUsers().size());
	}
}
