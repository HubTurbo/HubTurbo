package logic;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;

public class ResourceConverter {

	public static Issue convertToGhIssue(TurboIssue turboIssue) {
		Issue issue = turboIssue.getGhIssue();
		issue.setTitle(turboIssue.getTitle());
		issue.setBody(turboIssue.getDescription());
		issue.setMilestone(convertToGhMilestone(turboIssue.getMilestone()));
		issue.setLabels(convertToGhLabel(turboIssue.getLabels()));
		
		return issue;
	}
	
	public static Milestone convertToGhMilestone(TurboMilestone turboMilestone) {
		return turboMilestone.getGhMilestone();
	}
	
	public static Label converToGhLabel(TurboLabel turboLabel) {
		return turboLabel.getGhLabel();
	}
	
	public static List<Label> convertToGhLabel(List<TurboLabel> turboLabels) {
		List<Label> labels = new ArrayList<Label>();
		for (TurboLabel turboLabel : turboLabels) {
			labels.add(converToGhLabel(turboLabel));
		}
		return labels;
	}
}
