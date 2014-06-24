package model;

import java.util.ArrayList;
import java.util.List;

public class Model {
	
	public static final String MILESTONES_ALL = "all";
	public static final String MILESTONES_OPEN = "open";
	public static final String MILESTONES_CLOSED = "closed";
	
	private List<TurboCollaborator> collaborators = new ArrayList<TurboCollaborator>();
	private List<TurboIssue> issues = new ArrayList<TurboIssue>();
	private List<TurboLabel> labels = new ArrayList<TurboLabel>();
	private List<TurboMilestone> milestones = new ArrayList<TurboMilestone>(); 

	public List<TurboIssue> getIssues() {
		return this.issues;
	}

	public List<TurboCollaborator> getCollaborators() {
		return collaborators;
	}

	public List<TurboLabel> getLabels() {
		return labels;
	}

	public List<TurboMilestone> getMilestones() {
		return milestones;
	}
}
