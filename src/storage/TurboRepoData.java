package storage;

import java.util.ArrayList;
import java.util.List;

import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

public class TurboRepoData {
	
	//last ETags
	private String issuesETag = null;
	private String collabsETag = null;
	private String labelsETag = null;
	private String milestonesETag = null;
	private String issueCheckTime = null;
	private List<TurboUser> collaborators = null;
	private List<TurboSerializableLabel> labels = null;
	private List<TurboSerializableMilestone> milestones = null; 
	private List<TurboSerializableIssue> issues = null;
	
	public TurboRepoData(String issuesETag, String collabsETag, String labelsETag, String milestonesETag, String issueCheckTime, List<TurboUser> collaborators, List<TurboLabel> labels, List<TurboMilestone> milestones, List<TurboIssue> issues) {
		this.issuesETag = issuesETag;
		this.collabsETag = collabsETag;
		this.labelsETag = labelsETag;
		this.milestonesETag = milestonesETag;
		this.issueCheckTime = issueCheckTime;
		
		this.collaborators = collaborators;
		
		this.labels = new ArrayList<TurboSerializableLabel>();
		if (this.labels != null) {
			for (TurboLabel label : labels) {
				this.labels.add(new TurboSerializableLabel(label));
			}
		}
		
		this.milestones = new ArrayList<TurboSerializableMilestone>();
		if (this.milestones != null) {
			for (TurboMilestone milestone : milestones) {
				this.milestones.add(new TurboSerializableMilestone(milestone));
			}
		}

		this.issues = new ArrayList<TurboSerializableIssue>();
		if (this.issues != null) {
			for (TurboIssue issue : issues) {
				this.issues.add(new TurboSerializableIssue(issue));
			}
		}
	}
	
	public String getIssuesETag() {
		return issuesETag;
	}
	
	public String getCollaboratorsETag() {
		return collabsETag;
	}
	
	public String getLabelsETag() {
		return labelsETag;
	}
	
	public String getMilestonesETag() {
		return milestonesETag;
	}
	
	public String getIssueCheckTime() {
		return issueCheckTime;
	}
	
	public List<TurboUser> getCollaborators() {
		return collaborators;
	}
	
	public List<TurboLabel> getLabels() {
		List<TurboLabel> turboLabelList = new ArrayList<TurboLabel>();
		if (this.labels != null) {
			for (TurboSerializableLabel label: this.labels) {
				turboLabelList.add(label.toTurboLabel());
			}
		}
		return turboLabelList;
	}
	
	public List<TurboMilestone> getMilestones() {
		List<TurboMilestone> turboMilestoneList = new ArrayList<TurboMilestone>();
		if (this.milestones != null) {
			for (TurboSerializableMilestone milestone: this.milestones) {
				turboMilestoneList.add(milestone.toTurboMilestone());
			}
		}
		return turboMilestoneList;
	}
	
	public List<TurboIssue> getIssues(Model model) {
		List<TurboIssue> turboIssueList = new ArrayList<TurboIssue>();
		if (this.issues != null) {
			for (TurboSerializableIssue issue: this.issues) {
				turboIssueList.add(issue.toTurboIssue(model));
			}
		}
		return turboIssueList;
	}

	@Override
	public String toString() {
		return "Collaborators = " + collaborators + ", Labels = " + labels + ", Milestones = " + milestones + ", Issues = " + issues;
	}
}
