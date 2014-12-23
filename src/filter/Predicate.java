package filter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

public class Predicate implements FilterExpression {
	
	public static final Predicate EMPTY = new filter.Predicate("", "");

	private final String name;
	
	private String content = null;
	private DateRange dateRange = null;
	private LocalDate date = null;

	public Predicate(String name, String content) {
		this.name = name;
		this.content = content;
	}
	
	public Predicate(String name, DateRange dateRange) {
		this.name = name;
		this.dateRange = dateRange;
	}

	public Predicate(String name, LocalDate date) {
		this.name = name;
		this.date = date;
	}

	public boolean isSatisfiedBy(TurboIssue issue, Model model) {
		assert name != null && content != null;

		// The empty predicate is satisfied by anything
		if (name.isEmpty() && content.isEmpty()) return true;

		switch (name) {
		case "id":
			return idSatisfies(issue);
		case "title":
			return titleSatisfies(issue);
		case "milestone":
			return milestoneSatisfies(issue);
		case "parent":
			return parentSatisfies(issue, model);
		case "label":
			return labelsSatisfy(issue);
		case "assignee":
			return assigneeSatisfies(issue);
		case "state":
		case "status":
			return stateSatisfies(issue);
		case "has":
			return satisfiesHasConditions(issue);
		default:
			return false;
		}
	}

	@Override
	public void applyTo(TurboIssue issue, Model model) throws PredicateApplicationException {
		assert name != null && content != null;
		// The empty predicate should not be applied to anything
		assert !(name.isEmpty() && content.isEmpty());

		switch (name) {
		case "title":
			throw new PredicateApplicationException("Unnecessary filter: title cannot be changed by dragging");
		case "id":
			throw new PredicateApplicationException("Unnecessary filter: id is immutable");
		case "has":
			throw new PredicateApplicationException("Ambiguous filter: has");
		case "milestone":
			applyMilestone(issue, model);
			break;
		case "parent":
			applyParent(issue, model);
			break;
		case "label":
			applyLabel(issue, model);
			break;
		case "assignee":
			applyAssignee(issue, model);
			break;
		case "state":
		case "status":
			applyState(issue);
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
		return new ArrayList<String>(Arrays.asList(name));
	}

	@Override
	public String toString() {
		if (this == EMPTY) {
			return "<empty predicate>";
		} else if (name.equals("keyword")) {
			return content;
		} else {
			return name + ":" + content;
		}
	}

	// TODO update
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
	
	private int parseIdString(String id) {
		if (id.startsWith("#")) {
			return Integer.parseInt(id.substring(1));
		} else if (Character.isDigit(id.charAt(0))) {
			return Integer.parseInt(id);
		} else {
			return -1;
		}
	}

	private boolean idSatisfies(TurboIssue issue) {
		return issue.getId() == parseIdString(content);
	}

	private boolean satisfiesHasConditions(TurboIssue issue) {
		switch (content) {
		case "label":
			return issue.getLabels().size() > 0;
		case "milestone":
			return issue.getMilestone() != null;
		case "assignee":
			return issue.getAssignee() != null;
		case "parent":
			return issue.getParentIssue() != -1;
		default:
			return false;
		}
	}

	private boolean stateSatisfies(TurboIssue issue) {
		if (content.toLowerCase().contains("open")) {
			return issue.getOpen();
		} else if (content.toLowerCase().contains("closed")) {
			return !issue.getOpen();
		} else {
			return false;
		}
	}

	private boolean assigneeSatisfies(TurboIssue issue) {
		if (issue.getAssignee() == null) return false;
		return issue.getAssignee().getAlias().toLowerCase().contains(content.toLowerCase())
				|| issue.getAssignee().getGithubName().toLowerCase().contains(content.toLowerCase())
				|| (issue.getAssignee().getRealName() != null && issue.getAssignee().getRealName().toLowerCase().contains(content.toLowerCase()));
	}
	
	private boolean labelsSatisfy(TurboIssue issue) {
		String group = "";
		String labelName = content.toLowerCase();
		
//		if (content.contains(".")) {
//			if (content.length() == 1) {
//				// It's just a dot
//				return true;
//			}
//			int pos = content.indexOf('.');
//			group = content.substring(0, pos);
//			labelName = content.substring(pos+1);
//		}else if(content.contains("-")){
//			int pos = content.indexOf('-');
//			group = content.substring(0, pos);
//			labelName = content.substring(pos+1);
//		}
		
		String[] tokens = TurboLabel.parseName(labelName);
		if(tokens != null){
			group = tokens[0];
			labelName = tokens[1];
		}
		
		// Both can't be null
		assert group != null && labelName != null;
		// At most one can be empty
		assert !(group.isEmpty() && labelName.isEmpty());
		
		for (TurboLabel l : issue.getLabels()) {
			if (labelName.isEmpty() || l.getName() != null && l.getName().toLowerCase().contains(labelName)) {
				if (group.isEmpty() || l.getGroup() != null && l.getGroup().toLowerCase().contains(group)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean parentSatisfies(TurboIssue issue, Model model) {
		String parent = content.toLowerCase();
		int index = parseIdString(parent);
		if (index > 0) {
			TurboIssue current = issue;
			
			// The parent itself should show
			if (current.getId() == index) return true;
			
			// Descendants should show too
			return current.hasAncestor(index);
		}
		// Invalid issue number
		return false;
	}

	private boolean milestoneSatisfies(TurboIssue issue) {
		if (issue.getMilestone() == null) return false;
		return issue.getMilestone().getTitle().toLowerCase().contains(content.toLowerCase());
	}

	private boolean titleSatisfies(TurboIssue issue) {
		return issue.getTitle().toLowerCase().contains(content.toLowerCase());
	}

	private void applyMilestone(TurboIssue issue, Model model)
			throws PredicateApplicationException {
		// Find milestones containing the partial title
		List<TurboMilestone> milestones = model.getMilestones().stream().filter(m -> m.getTitle().toLowerCase().contains(content.toLowerCase())).collect(Collectors.toList());
		if (milestones.size() > 1) {
			throw new PredicateApplicationException("Ambiguous filter: can apply any of the following milestones: " + milestones.toString());
		} else {
			issue.setMilestone(milestones.get(0));
		}
	}

	private void applyParent(TurboIssue issue, Model model)
			throws PredicateApplicationException {
		String parent = content.toLowerCase();
		int index = parseIdString(parent);
		if (index != -1) {
			issue.setParentIssue(index);
		} else {
			// Find parents containing the partial title
			List<TurboIssue> parents = model.getIssues().stream().filter(i -> i.getTitle().toLowerCase().contains(parent.toLowerCase())).collect(Collectors.toList());
			if (parents.size() > 1) {
				throw new PredicateApplicationException("Ambiguous filter: can apply any of the following parents: " + parents.toString());
			} else {
				issue.setParentIssue(parents.get(0).getId());
			}
		}
	}

	private void applyLabel(TurboIssue issue, Model model)
			throws PredicateApplicationException {
		// Find labels containing the label name
		List<TurboLabel> labels = model.getLabels()
									   .stream()
									   .filter(l -> l.toGhName().toLowerCase().contains(content.toLowerCase())).collect(Collectors.toList());
		if (labels.size() > 1) {
			throw new PredicateApplicationException("Ambiguous filter: can apply any of the following labels: " + labels.toString());
		} else {
			issue.addLabel(labels.get(0));
		}
	}

	private void applyAssignee(TurboIssue issue, Model model)
			throws PredicateApplicationException {
		// Find assignees containing the partial title
		List<TurboUser> assignees = model.getCollaborators().stream().filter(c -> c.getGithubName().toLowerCase().contains(content.toLowerCase())).collect(Collectors.toList());
		if (assignees.size() > 1) {
			throw new PredicateApplicationException("Ambiguous filter: can apply any of the following assignees: " + assignees.toString());
		} else {
			issue.setAssignee(assignees.get(0));
		}
	}

	private void applyState(TurboIssue issue) {
		if (content.toLowerCase().contains("open")) {
			issue.setOpen(true);
		} else if (content.toLowerCase().contains("closed")) {
			issue.setOpen(false);
		}
	}
}