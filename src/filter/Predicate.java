package filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

public class Predicate implements FilterExpression {
	private String name;
	private String content;

	public Predicate(String name, String content) {
		this.name = name;
		this.content = content;
	}
	
	public Predicate() {
		this.name = null;
		this.content = null;
	}

	@Override
	public String toString() {
		return name + "(" + content + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Predicate other = (Predicate) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public boolean isSatisfiedBy(TurboIssue issue, Model model) {
		if (name == null && content == null) return true;
		
		switch (name) {
		case "title":
			return issue.getTitle().toLowerCase().contains(content.toLowerCase());
		case "milestone":
			if (issue.getMilestone() == null) return false;
			return issue.getMilestone().getTitle().toLowerCase().contains(content.toLowerCase());
		case "parent":
			content = content.toLowerCase();
			if (content.startsWith("#")) {
				return issue.getParents().contains(Integer.parseInt(content.substring(1)));
			} else if (Character.isDigit(content.charAt(0))) {
				return issue.getParents().contains(Integer.parseInt(content));
			} else {
				List<TurboIssue> actualParentInstances = model.getIssues().stream().filter(i -> issue.getParents().contains(i.getId())).collect(Collectors.toList());
				for (int i=0; i<actualParentInstances.size(); i++) {
					if (actualParentInstances.get(i).getTitle().toLowerCase().contains(content)) {
						return true;
					}
				}
				return false;
			}
		case "label":
			for (TurboLabel l : issue.getLabels()) {
				if (l.getName().toLowerCase().contains(content.toLowerCase())) {
					return true;
				}
			}
			return false;
		case "assignee":
			if (issue.getAssignee() == null) return false;
			return issue.getAssignee().getGithubName().toLowerCase().contains(content.toLowerCase())
					|| (issue.getAssignee().getRealName() != null && issue.getAssignee().getRealName().toLowerCase().contains(content.toLowerCase()));
		case "state":
		case "status":
			if (content.toLowerCase().contains("open")) {
				return issue.getOpen();
			} else if (content.toLowerCase().contains("closed")) {
				return !issue.getOpen();
			} else {
				return false;
			}
		default:
			return false;
		}
	}

	@Override
	public void applyTo(TurboIssue issue, Model model) throws PredicateApplicationException {
		assert !(name == null && content == null);
		
		switch (name) {
		case "title":
			issue.setTitle(content);
			break;
		case "milestone":
			// Find milestones containing the partial title
			List<TurboMilestone> milestones = model.getMilestones().stream().filter(m -> m.getTitle().toLowerCase().contains(content.toLowerCase())).collect(Collectors.toList());
			if (milestones.size() > 1) {
				throw new PredicateApplicationException("Ambiguous filter: can apply any of the following milestones: " + milestones.toString());
			} else {
				issue.setMilestone(milestones.get(0));
			}
			break;
		case "parent":
			content = content.toLowerCase();
			if (content.startsWith("#")) {
				issue.setParents(FXCollections.observableArrayList(Integer.parseInt(content.substring(1))));
			} else if (Character.isDigit(content.charAt(0))) {
				issue.setParents(FXCollections.observableArrayList(Integer.parseInt(content)));
			} else {
				// Find parents containing the partial title
				List<TurboIssue> parents = model.getIssues().stream().filter(i -> i.getTitle().toLowerCase().contains(content.toLowerCase())).collect(Collectors.toList());
				if (parents.size() > 1) {
					throw new PredicateApplicationException("Ambiguous filter: can apply any of the following parents: " + parents.toString());
				} else {
					issue.addParent(parents.get(0).getId());
				}
			}
			break;
		case "label":
			// Find labels containing the partial title
			List<TurboLabel> labels = model.getLabels().stream().filter(l -> l.getName().toLowerCase().contains(content.toLowerCase())).collect(Collectors.toList());
			if (labels.size() > 1) {
				throw new PredicateApplicationException("Ambiguous filter: can apply any of the following labels: " + labels.toString());
			} else {
				issue.addLabel(labels.get(0));
			}
			break;
		case "assignee":
			// Find assignees containing the partial title
			List<TurboUser> assignees = model.getCollaborators().stream().filter(c -> c.getGithubName().toLowerCase().contains(content.toLowerCase())).collect(Collectors.toList());
			if (assignees.size() > 1) {
				throw new PredicateApplicationException("Ambiguous filter: can apply any of the following assignees: " + assignees.toString());
			} else {
				issue.setAssignee(assignees.get(0));
			}
			break;
		case "state":
		case "status":
			if (content.toLowerCase().contains("open")) {
				issue.setOpen(true);
			} else if (content.toLowerCase().contains("closed")) {
				issue.setOpen(false);
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean canBeAppliedToIssue() {
		return true;
	}
	
	@Override
	public List<String> getPredicateNames() {
		return new ArrayList<String>(Arrays.asList(content));
	}
}