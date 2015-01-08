package filter.expression;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;
import util.Utility;
import filter.MetaQualifierInfo;
import filter.QualifierApplicationException;

public class Qualifier implements FilterExpression {
	
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yy, h:mm a");
	public static final Qualifier EMPTY = new Qualifier("", "");

	private final String name;
	
	// Only one of these will be present at a time
	private Optional<DateRange> dateRange = Optional.empty();
	private Optional<String> content = Optional.empty();
	private Optional<LocalDate> date = Optional.empty();
	private Optional<NumberRange> numberRange = Optional.empty();
	private Optional<Integer> number = Optional.empty();

	// Copy constructor
	public Qualifier(Qualifier other) {
		this.name = other.getName();
		if (other.getDateRange().isPresent()) {
			this.dateRange = other.getDateRange();
		} else if (other.getDate().isPresent()) {
			this.date = other.getDate();
		} else if (other.getContent().isPresent()) {
			this.content = other.getContent();
		} else if (other.getNumberRange().isPresent()) {
			this.numberRange = other.getNumberRange();
		} else if (other.getNumber().isPresent()) {
			this.number = other.getNumber();
		} else {
			assert false : "Unrecognised content type! You may have forgotten to add it above";
		}
	}
	
	public Qualifier(String name, String content) {
		this.name = name;
		this.content = Optional.of(content);
	}
	
	public Qualifier(String name, NumberRange numberRange) {
		this.name = name;
		this.numberRange = Optional.of(numberRange);
	}

	public Qualifier(String name, DateRange dateRange) {
		this.name = name;
		this.dateRange = Optional.of(dateRange);
	}

	public Qualifier(String name, LocalDate date) {
		this.name = name;
		this.date = Optional.of(date);
	}
	
	public Qualifier(String name, int number) {
		this.name = name;
		this.number = Optional.of(number);
	}
	
	/**
	 * Helper function for testing a filter expression against an issue.
	 * Ensures that meta-qualifiers are taken care of.
	 * Should always be used over isSatisfiedBy.
	 */
	public static boolean process(FilterExpression expr, TurboIssue issue) {
		
		FilterExpression exprWithNormalQualifiers = expr.filter(Qualifier::isNotMetaQualifier);
		List<Qualifier> metaQualifiers = expr.find(Qualifier::isMetaQualifier);
		
		return exprWithNormalQualifiers.isSatisfiedBy(issue, new MetaQualifierInfo(metaQualifiers));
	}
	
	public boolean isEmptyQualifier() {
		return name.isEmpty() && content.isPresent() && content.get().isEmpty();
	}

    public boolean isSatisfiedBy(TurboIssue issue, MetaQualifierInfo info) {
        assert name != null && content != null;

        // The empty qualifier is satisfied by anything
        if (isEmptyQualifier()) return true;

        switch (name) {
        case "id":
            return idSatisfies(issue);
        case "keyword":
            return keywordSatisfies(issue, info);
        case "title":
            return titleSatisfies(issue);
        case "body":
            return bodySatisfies(issue);
        case "milestone":
            return milestoneSatisfies(issue);
        case "parent":
            return parentSatisfies(issue);
        case "label":
            return labelsSatisfy(issue);
        case "author":
            return authorSatisfies(issue);
        case "assignee":
            return assigneeSatisfies(issue);
        case "involves":
        case "user":
            return involvesSatisfies(issue);
        case "type":
            return typeSatisfies(issue);
        case "state":
        case "status":
            return stateSatisfies(issue);
        case "has":
            return satisfiesHasConditions(issue);
        case "no":
            return satisfiesNoConditions(issue);
        case "is":
            return satisfiesIsConditions(issue);
        case "created":
            return satisfiesCreationDate(issue);
        case "updated":
            return satisfiesUpdatedHours(issue);
        default:
            return false;
        }
    }

	@Override
    public void applyTo(TurboIssue issue, Model model) throws QualifierApplicationException {
        assert name != null && content != null;
        
        // The empty qualifier should not be applied to anything
        assert !isEmptyQualifier();

        switch (name) {
        case "title":
        case "desc":
        case "body":
        case "keyword":
            throw new QualifierApplicationException("Unnecessary filter: issue text cannot be changed by dragging");
        case "id":
            throw new QualifierApplicationException("Unnecessary filter: id is immutable");
        case "created":
            throw new QualifierApplicationException("Unnecessary filter: cannot change issue creation date");
        case "has":
        case "no":
        case "is":
            throw new QualifierApplicationException("Ambiguous filter: " + name);
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
            throw new QualifierApplicationException("Unnecessary filter: cannot change author of issue");
        case "involves":
        case "user":
            throw new QualifierApplicationException("Ambiguous filter: cannot change users involved with issue");
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
    public List<String> getQualifierNames() {
        return new ArrayList<String>(Arrays.asList(name));
    }

	@Override
	public FilterExpression filter(Predicate<Qualifier> pred) {
		if (pred.test(this)) {
			return new Qualifier(this);
		} else {
			return EMPTY;
		}
	}
	
	@Override
	public List<Qualifier> find(Predicate<Qualifier> pred) {
		if (pred.test(this)) {
			ArrayList<Qualifier> result = new ArrayList<>();
			result.add(this);
			return result;
		} else {
			return new ArrayList<>();
		}
	}

	/**
     * This method is used to serialise qualifiers. Thus whatever form returned
     * should be syntactically valid.
     */
    @Override
    public String toString() {
        if (this == EMPTY) {
            return "";
        } else if (content.isPresent()) {
            if (name.equals("keyword")) {
                return content.get();
            } else {
                return name + ":" + content.get().toString();
            }
        } else if (date.isPresent()) {
            return name + ":" + date.get().toString();
        } else if (dateRange.isPresent()) {
            return name + ":" + dateRange.get().toString();
        } else if (numberRange.isPresent()) {
        	return name + ":" + numberRange.get().toString();
        } else if (number.isPresent()) {
        	return name + ":" + number.get().toString();
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
        Qualifier other = (Qualifier) obj;
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

	private static boolean isNotMetaQualifier(Qualifier q) {
		return !isMetaQualifier(q);
	}

	private static boolean isMetaQualifier(Qualifier q) {
		switch (q.getName()) {
		case "in":
			return true;
		default:
			return false;
		}
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

    private boolean satisfiesUpdatedHours(TurboIssue issue) {
    	int hours = Utility.safeLongToInt(issue.getUpdatedAt()
    			.until(LocalDateTime.now(), ChronoUnit.HOURS));

    	if (numberRange.isPresent()) {
    		return numberRange.get().encloses(hours);
    	} else if (number.isPresent()) {
    		// Treat it as <
    		return new NumberRange(null, number.get(), true).encloses(hours);
    	} else {
    		return false;
    	}
	}
    
    private boolean satisfiesCreationDate(TurboIssue issue) {
    	LocalDate creationDate = LocalDate.parse(issue.getCreatedAt(), formatter);
    	if (date.isPresent()) {
    		return creationDate.isEqual(date.get());
    	} else if (dateRange.isPresent()) {
    		return dateRange.get().encloses(creationDate);
    	} else {
    		return false;
    	}
	}

	private boolean satisfiesHasConditions(TurboIssue issue) {
    	if (!content.isPresent()) return false;
        switch (content.get()) {
        case "label":
        case "labels":
            return issue.getLabels().size() > 0;
        case "milestone":
        case "milestones":
            return issue.getMilestone() != null;
        case "assignee":
        case "assignees":
            return issue.getAssignee() != null;
        case "parent":
        case "parents":
            return issue.getParentIssue() != -1;
        default:
            return false;
        }
    }

    private boolean satisfiesNoConditions(TurboIssue issue) {
    	if (!content.isPresent()) return false;
        return !satisfiesHasConditions(issue);
    }

	private boolean satisfiesIsConditions(TurboIssue issue) {
    	if (!content.isPresent()) return false;
        switch (content.get()) {
        case "open":
        case "closed":
            return stateSatisfies(issue);
        case "pr":
        case "issue":
            return typeSatisfies(issue);
        case "merged":
        case "unmerged":
        	return isPullRequest(issue) && !issue.isOpen();
        default:
            return false;
        }
    }

	private boolean stateSatisfies(TurboIssue issue) {
    	if (!content.isPresent()) return false;
    	String content = this.content.get().toLowerCase();
        if (content.contains("open")) {
            return issue.isOpen();
        } else if (content.contains("closed")) {
            return !issue.isOpen();
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
    
    private boolean involvesSatisfies(TurboIssue issue) {
    	return authorSatisfies(issue) || assigneeSatisfies(issue);
    }

    private boolean labelsSatisfy(TurboIssue issue) {
    	if (!content.isPresent()) return false;
        String group = "";
        String labelName = content.get().toLowerCase();
        
        Optional<String[]> tokens = TurboLabel.parseName(labelName);
		if (tokens.isPresent()) {
			group = tokens.get()[0];
			labelName = tokens.get()[1];
		} else {
			// The name isn't in the format group.name or group.
			// Take the entire thing to be the label name
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

    private boolean parentSatisfies(TurboIssue issue) {
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

    private boolean keywordSatisfies(TurboIssue issue, MetaQualifierInfo info) {
    	
    	if (info.getIn().isPresent()) {
    		switch (info.getIn().get()) {
    		case "title":
    	        return titleSatisfies(issue);
    		case "body":
    		case "desc":
    	        return bodySatisfies(issue);
    	    default:
    	    	return false;
    		}
    	} else {
	        return titleSatisfies(issue) || bodySatisfies(issue);
    	}
	}

	private boolean bodySatisfies(TurboIssue issue) {
    	if (!content.isPresent()) return false;
        return issue.getDescription().toLowerCase().contains(content.get().toLowerCase());
    }

	private boolean titleSatisfies(TurboIssue issue) {
    	if (!content.isPresent()) return false;
        return issue.getTitle().toLowerCase().contains(content.get().toLowerCase());
    }

    private boolean isPullRequest(TurboIssue issue) {
    	return issue.getPullRequest() != null;
    }
    
    private boolean typeSatisfies(TurboIssue issue) {
    	if (!content.isPresent()) return false;
    	String content = this.content.get().toLowerCase();
    	if (content.equals("issue")) {
            return !isPullRequest(issue);
    	} else if (content.equals("pr") || content.equals("pullrequest")) {
    		return isPullRequest(issue);
    	} else {
    		return false;
    	}
	}

	private void applyMilestone(TurboIssue issue, Model model) throws QualifierApplicationException {
    	if (!content.isPresent()) {
    		throw new QualifierApplicationException("Invalid milestone " + (date.isPresent() ? date.get() : dateRange.get()));
    	}
    	
        // Find milestones containing the partial title
        List<TurboMilestone> milestones = model.getMilestones().stream().filter(m -> m.getTitle().toLowerCase().contains(content.get().toLowerCase())).collect(Collectors.toList());
        if (milestones.size() > 1) {
            throw new QualifierApplicationException("Ambiguous filter: can apply any of the following milestones: " + milestones.toString());
        } else {
            issue.setMilestone(milestones.get(0));
        }
    }

    private void applyParent(TurboIssue issue, Model model) throws QualifierApplicationException {
    	if (!content.isPresent()) {
    		throw new QualifierApplicationException("Invalid parent " + (date.isPresent() ? date.get() : dateRange.get()));
    	}
        String parent = content.get().toLowerCase();
        int index = parseIdString(parent);
        if (index != -1) {
            issue.setParentIssue(index);
        } else {
            // Find parents containing the partial title
            List<TurboIssue> parents = model.getIssues().stream().filter(i -> i.getTitle().toLowerCase().contains(parent.toLowerCase())).collect(Collectors.toList());
            if (parents.size() > 1) {
                throw new QualifierApplicationException("Ambiguous filter: can apply any of the following parents: " + parents.toString());
            } else {
                issue.setParentIssue(parents.get(0).getId());
            }
        }
    }

    private void applyLabel(TurboIssue issue, Model model) throws QualifierApplicationException {
    	if (!content.isPresent()) {
    		throw new QualifierApplicationException("Invalid label " + (date.isPresent() ? date.get() : dateRange.get()));
    	}

        // Find labels containing the label name
        List<TurboLabel> labels = model.getLabels()
                                       .stream()
                                       .filter(l -> l.toGhName().toLowerCase().contains(content.get().toLowerCase())).collect(Collectors.toList());
        if (labels.size() > 1) {
            throw new QualifierApplicationException("Ambiguous filter: can apply any of the following labels: " + labels.toString());
        } else {
            issue.addLabel(labels.get(0));
        }
    }

    private void applyAssignee(TurboIssue issue, Model model) throws QualifierApplicationException {
    	if (!content.isPresent()) {
    		throw new QualifierApplicationException("Invalid assignee " + (date.isPresent() ? date.get() : dateRange.get()));
    	}

        // Find assignees containing the partial title
        List<TurboUser> assignees = model.getCollaborators().stream().filter(c -> c.getGithubName().toLowerCase().contains(content.get().toLowerCase())).collect(Collectors.toList());
        if (assignees.size() > 1) {
            throw new QualifierApplicationException("Ambiguous filter: can apply any of the following assignees: " + assignees.toString());
        } else {
            issue.setAssignee(assignees.get(0));
        }
    }

    private void applyState(TurboIssue issue) throws QualifierApplicationException {
    	if (!content.isPresent()) {
    		throw new QualifierApplicationException("Invalid state " + (date.isPresent() ? date.get() : dateRange.get()));
    	}

        if (content.get().toLowerCase().contains("open")) {
            issue.setOpen(true);
        } else if (content.get().toLowerCase().contains("closed")) {
            issue.setOpen(false);
        }
    }
    
	public Optional<Integer> getNumber() {
		return number;
	}

	public Optional<NumberRange> getNumberRange() {
		return numberRange;
	}

	public Optional<DateRange> getDateRange() {
		return dateRange;
	}

	public Optional<String> getContent() {
		return content;
	}

	public Optional<LocalDate> getDate() {
		return date;
	}

	public String getName() {
		return name;
	}
}