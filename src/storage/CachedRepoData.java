package storage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

public class CachedRepoData {
	
	//last ETags
	private String issuesETag = null;
	private String collabsETag = null;
	private String labelsETag = null;
	private String milestonesETag = null;
	private LocalDateTime issueCheckTime = null;
	private List<TurboUser> collaborators = null;
	private List<SerializableLabel> labels = null;
	private List<SerializableMilestone> milestones = null; 
	private List<SerializableIssue> issues = null;
	
	public CachedRepoData(String issuesETag, String collabsETag, String labelsETag, String milestonesETag,
	                      LocalDateTime issueCheckTime, List<TurboUser> collaborators, List<TurboLabel> labels,
	                      List<TurboMilestone> milestones, List<TurboIssue> issues) {

		this.issuesETag = issuesETag;
		this.collabsETag = collabsETag;
		this.labelsETag = labelsETag;
		this.milestonesETag = milestonesETag;
		this.issueCheckTime = issueCheckTime;
		
		this.collaborators = collaborators;
		
		this.labels = new ArrayList<SerializableLabel>();
		if (this.labels != null) {
			for (TurboLabel label : labels) {
				this.labels.add(new SerializableLabel(label));
			}
		}
		
		this.milestones = new ArrayList<SerializableMilestone>();
		if (this.milestones != null) {
			for (TurboMilestone milestone : milestones) {
				this.milestones.add(new SerializableMilestone(milestone));
			}
		}

		this.issues = new ArrayList<SerializableIssue>();
		if (this.issues != null) {
			for (TurboIssue issue : issues) {
				this.issues.add(new SerializableIssue(issue));
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
	
	public LocalDateTime getIssueCheckTime() {
		return issueCheckTime;
	}
	
	public List<TurboUser> getCollaborators() {
		if (this.collaborators == null) {
			return new ArrayList<TurboUser>();
		} else {
			return this.collaborators;
		}
	}
	
	public List<TurboLabel> getLabels() {
		List<TurboLabel> turboLabelList = new ArrayList<TurboLabel>();
		if (this.labels != null) {
			for (SerializableLabel label: this.labels) {
				turboLabelList.add(label.toTurboLabel());
			}
		}
		return turboLabelList;
	}
	
	public List<TurboMilestone> getMilestones() {
		List<TurboMilestone> turboMilestoneList = new ArrayList<TurboMilestone>();
		if (this.milestones != null) {
			for (SerializableMilestone milestone: this.milestones) {
				turboMilestoneList.add(milestone.toTurboMilestone());
			}
		}
		return turboMilestoneList;
	}
	
	public List<TurboIssue> getIssues(Model model) {
		List<TurboIssue> turboIssueList = new ArrayList<TurboIssue>();
		if (this.issues != null) {
			for (SerializableIssue issue: this.issues) {
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
