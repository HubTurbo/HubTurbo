package filter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

public class Predicate implements FilterExpression {
	
	public static final Predicate EMPTY = new filter.Predicate("", "");

	private final String name;
	
	// Only one of these will be present at a time
	private Optional<DateRange> dateRange = Optional.empty();
	private Optional<String> content = Optional.empty();
	private Optional<LocalDate> date = Optional.empty();

	public Predicate(String name, String content) {
		this.name = name;
		this.content = Optional.of(content);
	}
	
	public Predicate(String name, DateRange dateRange) {
		this.name = name;
		this.dateRange = Optional.of(dateRange);
	}

	public Predicate(String name, LocalDate date) {
		this.name = name;
		this.date = Optional.of(date);
	}
	
	public boolean isEmptyPredicate() {
		return name.isEmpty() && content.isPresent() && content.get().isEmpty();
	}

    public boolean isSatisfiedBy(TurboIssue issue, Model model) {
        assert name != null && content != null;

        // The empty predicate is satisfied by anything
        if (isEmptyPredicate()) return true;

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
        case "author":
            return authorSatisfies(issue);
        case "assignee":
            return assigneeSatisfies(issue);
        case "type":
            return typeSatisfies(issue);
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
        assert !isEmptyPredicate();

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
        case "author":
            throw new PredicateApplicationException("Unnecessary filter: cannot change author of issue");
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
        } else if (content != null) {
            if (name.equals("keyword")) {
                return content.get();
            } else {
                return name + ":" + content.get().toString();
            }
        } else if (date != null) {
            return name + ":" + date.get().toString();
        } else if (dateRange != null) {
            return name + ":" + dateRange.get().toString();
        } else {
            assert false : "Should not happen";
            return "";
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((dateRange == null) ? 0 : dateRange.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
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
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (dateRange == null) {
            if (other.dateRange != null)
                return false;
        } else if (!dateRange.equals(other.dateRange))
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
        if (!content.isPresent()) return false;
        return issue.getId() == parseIdString(content.get());
    }

    private boolean satisfiesHasConditions(TurboIssue issue) {
    	if (!content.isPresent()) return false;
        switch (content.get()) {
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
    	if (!content.isPresent()) return false;
        if (content.get().toLowerCase().contains("open")) {
            return issue.getOpen();
        } else if (content.get().toLowerCase().contains("closed")) {
            return !issue.getOpen();
        } else {
            return false;
        }
    }

    private boolean assigneeSatisfies(TurboIssue issue) {
    	if (!content.isPresent()) return false;
        TurboUser assignee = issue.getAssignee();
        String content = this.content.get().toLowerCase();
    
        if (assignee == null) return false;
        return assignee.getAlias().toLowerCase().contains(content)
                || assignee.getGithubName().toLowerCase().contains(content)
                || (assignee.getRealName() != null && assignee.getRealName().toLowerCase().contains(content));
    }
    
    private boolean authorSatisfies(TurboIssue issue) {
    	if (!content.isPresent()) return false;
        String creator = issue.getCreator().toLowerCase();
        return creator != null && creator.contains(content.get().toLowerCase());
    }

    private boolean labelsSatisfy(TurboIssue issue) {
    	if (!content.isPresent()) return false;
        String group = "";
        String labelName = content.get().toLowerCase();
        
//      if (content.contains(".")) {
//          if (content.length() == 1) {
//              // It's just a dot
//              return true;
//          }
//          int pos = content.indexOf('.');
//          group = content.substring(0, pos);
//          labelName = content.substring(pos+1);
//      }else if(content.contains("-")){
//          int pos = content.indexOf('-');
//          group = content.substring(0, pos);
//          labelName = content.substring(pos+1);
//      }
        
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
    	if (!content.isPresent()) return false;
    	String parent = content.get().toLowerCase();
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
    	if (!content.isPresent()) return false;
        if (issue.getMilestone() == null) return false;
        return issue.getMilestone().getTitle().toLowerCase().contains(content.get().toLowerCase());
    }

    private boolean titleSatisfies(TurboIssue issue) {
    	if (!content.isPresent()) return false;
        return issue.getTitle().toLowerCase().contains(content.get().toLowerCase());
    }

    private boolean typeSatisfies(TurboIssue issue) {
    	if (!content.isPresent()) return false;
    	String content = this.content.get().toLowerCase();
    	if (content.equals("issue")) {
            return issue.getPullRequest() == null;
    	} else if (content.equals("pr") || content.equals("pullrequest")) {
    		return issue.getPullRequest() != null;
    	} else {
    		return false;
    	}
	}

	private void applyMilestone(TurboIssue issue, Model model) throws PredicateApplicationException {
    	if (!content.isPresent()) {
    		throw new PredicateApplicationException("Invalid milestone " + (date.isPresent() ? date.get() : dateRange.get()));
    	}
    	
        // Find milestones containing the partial title
        List<TurboMilestone> milestones = model.getMilestones().stream().filter(m -> m.getTitle().toLowerCase().contains(content.get().toLowerCase())).collect(Collectors.toList());
        if (milestones.size() > 1) {
            throw new PredicateApplicationException("Ambiguous filter: can apply any of the following milestones: " + milestones.toString());
        } else {
            issue.setMilestone(milestones.get(0));
        }
    }

    private void applyParent(TurboIssue issue, Model model) throws PredicateApplicationException {
    	if (!content.isPresent()) {
    		throw new PredicateApplicationException("Invalid parent " + (date.isPresent() ? date.get() : dateRange.get()));
    	}
        String parent = content.get().toLowerCase();
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

    private void applyLabel(TurboIssue issue, Model model) throws PredicateApplicationException {
    	if (!content.isPresent()) {
    		throw new PredicateApplicationException("Invalid label " + (date.isPresent() ? date.get() : dateRange.get()));
    	}

        // Find labels containing the label name
        List<TurboLabel> labels = model.getLabels()
                                       .stream()
                                       .filter(l -> l.toGhName().toLowerCase().contains(content.get().toLowerCase())).collect(Collectors.toList());
        if (labels.size() > 1) {
            throw new PredicateApplicationException("Ambiguous filter: can apply any of the following labels: " + labels.toString());
        } else {
            issue.addLabel(labels.get(0));
        }
    }

    private void applyAssignee(TurboIssue issue, Model model) throws PredicateApplicationException {
    	if (!content.isPresent()) {
    		throw new PredicateApplicationException("Invalid assignee " + (date.isPresent() ? date.get() : dateRange.get()));
    	}

        // Find assignees containing the partial title
        List<TurboUser> assignees = model.getCollaborators().stream().filter(c -> c.getGithubName().toLowerCase().contains(content.get().toLowerCase())).collect(Collectors.toList());
        if (assignees.size() > 1) {
            throw new PredicateApplicationException("Ambiguous filter: can apply any of the following assignees: " + assignees.toString());
        } else {
            issue.setAssignee(assignees.get(0));
        }
    }

    private void applyState(TurboIssue issue) throws PredicateApplicationException {
    	if (!content.isPresent()) {
    		throw new PredicateApplicationException("Invalid state " + (date.isPresent() ? date.get() : dateRange.get()));
    	}

        if (content.get().toLowerCase().contains("open")) {
            issue.setOpen(true);
        } else if (content.get().toLowerCase().contains("closed")) {
            issue.setOpen(false);
        }
    }
}