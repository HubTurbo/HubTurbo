package util.events;

import java.util.ArrayList;
import java.util.List;

import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

public class ModelChangedEvent extends Event {
    public List<TurboIssue> issues = new ArrayList<>();
    public List<TurboMilestone> milestones = new ArrayList<>();
    public List<TurboLabel> labels = new ArrayList<>();
    public List<TurboUser> collaborators = new ArrayList<>();

    public ModelChangedEvent(List<TurboIssue> issues, List<TurboMilestone> milestones, List<TurboLabel> labels, List<TurboUser> collaborators) {
    	this.issues = issues;
    	this.labels = labels;
    	this.milestones = milestones;
    	this.collaborators = collaborators;
	}
}
