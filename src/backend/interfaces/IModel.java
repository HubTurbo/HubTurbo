package backend.interfaces;

import backend.resource.*;

import java.util.List;

public interface IModel {
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
