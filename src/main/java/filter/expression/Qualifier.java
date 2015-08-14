package filter.expression;

import backend.interfaces.IModel;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import filter.MetaQualifierInfo;
import filter.QualifierApplicationException;
import util.Utility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Qualifier implements FilterExpression {

    public static final String UPDATED = "updated";
    public static final String REPO = "repo";
    public static final String SORT = "sort";

    public static final Qualifier EMPTY = new Qualifier("", "");

    public static final List<String> KEYWORDS = Collections.unmodifiableList(Arrays.asList(
        "assignees", "author", "body", "closed", "comments", "created", "creator",
        "date", "nonSelfUpdate", "desc", "description", "has", "id", "in", "involves",
        "is", "issue", "keyword", "label", "labels", "merged", "milestone", "milestones",
        "no", "open", "pr", "pullrequest", "read", "repo", "sort", "state", "status",
        "title", "type", "unmerged", "unread", "updated", "user"
    ));

    private final String name;

    // Only one of these will be present at a time
    private Optional<DateRange> dateRange = Optional.empty();
    private Optional<String> content = Optional.empty();
    private Optional<LocalDate> date = Optional.empty();
    private Optional<NumberRange> numberRange = Optional.empty();
    private Optional<Integer> number = Optional.empty();
    private List<SortKey> sortKeys = new ArrayList<>();

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
        } else if (!other.sortKeys.isEmpty()) {
            this.sortKeys = new ArrayList<>(other.sortKeys);
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

    public Qualifier(String name, List<SortKey> keys) {
        this.name = name;
        this.sortKeys = new ArrayList<>(keys);
    }

    /**
     * Helper function for testing a filter expression against an issue.
     * Ensures that meta-qualifiers are taken care of.
     * Should always be used over isSatisfiedBy.
     */
    public static boolean process(IModel model, FilterExpression expr, TurboIssue issue) {
        FilterExpression exprWithNormalQualifiers = expr.filter(Qualifier::shouldNotBeStripped);
        List<Qualifier> metaQualifiers = expr.find(Qualifier::isMetaQualifier);

        // Preprocessing for repo qualifier
        boolean containsRepoQualifier = metaQualifiers.stream()
                .map(Qualifier::getName)
                .collect(Collectors.toList())
            .contains("repo");

        if (!containsRepoQualifier) {
            exprWithNormalQualifiers = new Conjunction(
                new Qualifier("repo", model.getDefaultRepo()),
                exprWithNormalQualifiers);
        }

        return exprWithNormalQualifiers.isSatisfiedBy(model, issue, new MetaQualifierInfo(metaQualifiers));
    }

    public static void processMetaQualifierEffects(FilterExpression expr,
                                                   BiConsumer<Qualifier, MetaQualifierInfo> callback) {

        List<Qualifier> qualifiers = expr.find(Qualifier::isMetaQualifier);
        MetaQualifierInfo info = new MetaQualifierInfo(qualifiers);
        qualifiers.forEach(q -> callback.accept(q, info));
    }

    private static LocalDateTime currentTime = null;

    private static LocalDateTime getCurrentTime() {
        if (currentTime == null) {
            return LocalDateTime.now();
        } else {
            return currentTime;
        }
    }

    /**
     * For testing. Stubs the current time so time-related qualifiers work properly.
     */
    public static void setCurrentTime(LocalDateTime dateTime) {
        currentTime = dateTime;
    }

    public boolean isEmptyQualifier() {
        return name.isEmpty() && content.isPresent() && content.get().isEmpty();
    }

    @Override
    public boolean isSatisfiedBy(IModel model, TurboIssue issue, MetaQualifierInfo info) {
        assert name != null;

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
        case "desc":
        case "description":
            return bodySatisfies(issue);
        case "milestone":
            return milestoneSatisfies(model, issue);
        case "label":
            return labelsSatisfy(model, issue);
        case "author":
        case "creator":
            return authorSatisfies(issue);
        case "assignee":
            return assigneeSatisfies(model, issue);
        case "involves":
        case "user":
            return involvesSatisfies(model, issue);
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
        case "repo":
            return satisfiesRepo(issue);
        default:
            return false;
        }
    }

    @Override
    public void applyTo(TurboIssue issue, IModel model) throws QualifierApplicationException {
        assert name != null && content != null;

        // The empty qualifier should not be applied to anything
        assert !isEmptyQualifier();

        switch (name) {
        case "title":
        case "desc":
        case "description":
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
        case "label":
            applyLabel(issue, model);
            break;
        case "assignee":
            applyAssignee(issue, model);
            break;
        case "author":
        case "creator":
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
        return new ArrayList<>(Arrays.asList(name));
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
            String quotedContent = content.get();
            if (quotedContent.contains(" ")) {
                quotedContent = "\"" + quotedContent + "\"";
            }
            if (name.equals("keyword")) {
                return quotedContent;
            } else {
                return name + ":" + quotedContent;
            }
        } else if (date.isPresent()) {
            return name + ":" + date.get().toString();
        } else if (dateRange.isPresent()) {
            return name + ":" + dateRange.get().toString();
        } else if (!sortKeys.isEmpty()) {
            return name + ":" + sortKeys.stream().map(SortKey::toString).collect(Collectors.joining(","));
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
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((dateRange == null) ? 0 : dateRange.hashCode());
        result = prime * result + ((number == null) ? 0 : number.hashCode());
        result = prime * result + ((numberRange == null) ? 0 : numberRange.hashCode());
        result = prime * result + ((sortKeys == null) ? 0 : sortKeys.hashCode());
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
        return content.equals(other.content) &&
                date.equals(other.date) &&
                dateRange.equals(other.dateRange) &&
                number.equals(other.number) &&
                numberRange.equals(other.numberRange) &&
                sortKeys.equals(other.sortKeys) &&
                name.equals(other.name);
    }

    private static boolean shouldNotBeStripped(Qualifier q) {
        return !shouldBeStripped(q);
    }

    private static boolean shouldBeStripped(Qualifier q) {
        switch (q.getName()) {
            case "in":
            case "sort":
                return true;
            default:
                return false;
        }
    }

    public static boolean isMetaQualifier(Qualifier q) {
        switch (q.getName()) {
        case "sort":
        case "in":
        case "repo":
        case "updated":
            return true;
        default:
            return false;
        }
    }

    public Comparator<TurboIssue> getCompoundSortComparator(IModel model, boolean isSortableByNonSelfUpdates) {
        if (sortKeys.isEmpty()) {
            return (a, b) -> 0;
        }
        return (a, b) -> {
            for (SortKey key : sortKeys) {
                Comparator<TurboIssue> comparator =
                        getSortComparator(model, key.key, key.inverted, isSortableByNonSelfUpdates);
                int result = comparator.compare(a, b);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        };
    }

    public static Comparator<TurboIssue> getSortComparator(IModel model,
                                                           String key,
                                                           boolean inverted,
                                                           boolean isSortableByNonSelfUpdates) {
        Comparator<TurboIssue> comparator = (a, b) -> 0;

        boolean isLabelGroup = false;

        switch (key) {
            case "comments":
                comparator = (a, b) -> a.getCommentCount() - b.getCommentCount();
                break;
            case "repo":
                comparator = (a, b) -> a.getRepoId().compareTo(b.getRepoId());
                break;
            case "updated":
            case "date":
                comparator = (a, b) -> a.getUpdatedAt().compareTo(b.getUpdatedAt());
                break;
            case "nonSelfUpdate":
                if (isSortableByNonSelfUpdates) {
                    comparator = (a, b) ->
                        a.getMetadata().getNonSelfUpdatedAt().compareTo(b.getMetadata().getNonSelfUpdatedAt());
                } else {
                    comparator = (a, b) -> a.getUpdatedAt().compareTo(b.getUpdatedAt());
                }
                break;
            case "id":
                comparator = (a, b) -> a.getId() - b.getId();
                break;
            default:
                // Doesn't match anything; assume it's a label group
                isLabelGroup = true;
                break;
        }

        if (isLabelGroup) {
            // Has a different notion of inversion
            return getLabelGroupComparator(model, key, inverted);
        } else {
            // Use default behaviour for inverting
            if (!inverted) {
                return comparator;
            } else {
                final Comparator<TurboIssue> finalComparator = comparator;
                return (a, b) -> -finalComparator.compare(a, b);
            }
        }
    }

    public static Comparator<TurboIssue> getLabelGroupComparator(IModel model, String key, boolean inverted) {
        // Strip trailing ., if any
        final String group = key.replaceAll("\\.$", "");
        return (a, b) -> {

            // Matches labels belong to the given group
            Predicate<TurboLabel> sameGroup = l ->
                l.getGroup().isPresent() && l.getGroup().get().equals(group);

            Comparator<TurboLabel> labelComparator = (x, y) -> x.getName().compareTo(y.getName());

            List<TurboLabel> aLabels = model.getLabelsOfIssue(a, sameGroup);
            List<TurboLabel> bLabels = model.getLabelsOfIssue(b, sameGroup);
            Collections.sort(aLabels, labelComparator);
            Collections.sort(bLabels, labelComparator);

            // Put empty lists at the back
            if (aLabels.size() == 0 && bLabels.size() == 0) {
                return 0;
            } else if (aLabels.size() == 0) {
                // a is larger
                return 1;
            } else if (bLabels.size() == 0) {
                // b is larger
                return -1;
            }

            // Compare lengths
            int result = !inverted
                ? aLabels.size() - bLabels.size()
                : bLabels.size() - aLabels.size();

            if (result != 0) {
                return result;
            }

            // Lexicographic label comparison
            assert aLabels.size() == bLabels.size();
            for (int i = 0; i < aLabels.size(); i++) {
                result = !inverted
                    ? labelComparator.compare(aLabels.get(i), bLabels.get(i))
                    : labelComparator.compare(bLabels.get(i), aLabels.get(i));
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        };
    }

    private boolean idSatisfies(TurboIssue issue) {
        if (number.isPresent()) {
            return issue.getId() == number.get();
        } else if (numberRange.isPresent()) {
            return numberRange.get().encloses(issue.getId());
        }
        return false;
    }

    private boolean satisfiesUpdatedHours(TurboIssue issue) {
        NumberRange updatedRange;

        if (numberRange.isPresent()) {
            updatedRange = numberRange.get();
        } else if (number.isPresent()) {
            updatedRange = new NumberRange(null, number.get(), true);
        } else {
            return false;
        }

        int hoursSinceUpdate;

        if (issue.getMetadata().isUpdated()) {
            // Second time being filtered, we now have metadata from source, so we can use getNonSelfUpdatedAt.
            hoursSinceUpdate = Utility.safeLongToInt(issue.getMetadata().getNonSelfUpdatedAt()
                    .until(getCurrentTime(), ChronoUnit.HOURS));
        } else {
            // First time being filtered (haven't gotten metadata from source yet).
            hoursSinceUpdate = Utility.safeLongToInt(issue.getUpdatedAt().until(getCurrentTime(), ChronoUnit.HOURS));
        }

        return updatedRange.encloses(hoursSinceUpdate);
    }

    private boolean satisfiesRepo(TurboIssue issue) {
        if (!content.isPresent()) return false;
        return issue.getRepoId().equalsIgnoreCase(content.get());
    }

    private boolean satisfiesCreationDate(TurboIssue issue) {
        LocalDate creationDate = issue.getCreatedAt().toLocalDate();
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
            assert issue.getMilestone() != null;
            return issue.getMilestone().isPresent();
        case "assignee":
        case "assignees":
            assert issue.getMilestone() != null;
            return issue.getAssignee().isPresent();
        default:
            return false;
        }
    }

    private boolean satisfiesNoConditions(TurboIssue issue) {
        return content.isPresent() && !satisfiesHasConditions(issue);
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
            return issue.isPullRequest() && !issue.isOpen();
        case "unmerged":
            return issue.isPullRequest() && issue.isOpen();
        case "read":
            return issue.isCurrentlyRead();
        case "unread":
            return !issue.isCurrentlyRead();
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

    private boolean assigneeSatisfies(IModel model, TurboIssue issue) {
        if (!content.isPresent()) return false;
        Optional<TurboUser> assignee = model.getAssigneeOfIssue(issue);

        if (!assignee.isPresent()) return false;

        String content = this.content.get().toLowerCase();
        String login = assignee.get().getLoginName() == null ? "" : assignee.get().getLoginName().toLowerCase();
        String name = assignee.get().getRealName() == null ? "" : assignee.get().getRealName().toLowerCase();

        return login.contains(content) || name.contains(content);
    }

    private boolean authorSatisfies(TurboIssue issue) {
        if (!content.isPresent()) return false;

        String creator = issue.getCreator();

        return creator.toLowerCase().contains(content.get().toLowerCase());
    }

    private boolean involvesSatisfies(IModel model, TurboIssue issue) {
        return authorSatisfies(issue) || assigneeSatisfies(model, issue);
    }

    private boolean labelsSatisfy(IModel model, TurboIssue issue) {
        if (!content.isPresent()) return false;

        // Make use of TurboLabel constructor to parse the string, to avoid duplication
        TurboLabel tokens = new TurboLabel("", content.get().toLowerCase());

        String group = "";
        if (tokens.getGroup().isPresent()) {
            group = tokens.getGroup().get().toLowerCase();
        }
        String labelName = tokens.getName().toLowerCase();

        for (TurboLabel label : model.getLabelsOfIssue(issue)) {
            if (label.getGroup().isPresent()) {
                if (labelName.isEmpty()) {
                    // Check the group
                    if (label.getGroup().get().toLowerCase().contains(group)) {
                       return true;
                    }
                } else {
                    if (label.getGroup().get().toLowerCase().contains(group)
                        && label.getName().toLowerCase().contains(labelName)) {
                       return true;
                    }
                }
            } else {
                // Check only the label name
                if (!group.isEmpty()) {
                    return false;
                } else if (!labelName.isEmpty() && label.getName().toLowerCase().contains(labelName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean milestoneSatisfies(IModel model, TurboIssue issue) {
        if (!content.isPresent()) return false;
        Optional<TurboMilestone> milestone = model.getMilestoneOfIssue(issue);

        if (!milestone.isPresent()) return false;

        String contents = content.get().toLowerCase();
        String title = milestone.get().getTitle().toLowerCase();

        return title.contains(contents);
    }

    private boolean keywordSatisfies(TurboIssue issue, MetaQualifierInfo info) {

        if (info.getIn().isPresent()) {
            switch (info.getIn().get()) {
            case "title":
                return titleSatisfies(issue);
            case "body":
            case "desc":
            case "description":
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

    private boolean typeSatisfies(TurboIssue issue) {
        if (!content.isPresent()) return false;
        String content = this.content.get().toLowerCase();
        switch (content) {
            case "issue":
                return !issue.isPullRequest();
            case "pr":
            case "pullrequest":
                return issue.isPullRequest();
            default:
                return false;
        }
    }

    private void applyMilestone(TurboIssue issue, IModel model) throws QualifierApplicationException {
        if (!content.isPresent()) {
            throw new QualifierApplicationException("Name of milestone to apply required");
        }

        // Find milestones containing partial title
        List<TurboMilestone> milestones = model.getMilestones().stream()
            .filter(m -> m.getTitle().toLowerCase().contains(content.get().toLowerCase()))
            .collect(Collectors.toList());

        if (milestones.isEmpty()) {
            throw new QualifierApplicationException("Invalid milestone " + content.get());
        } else if (milestones.size() == 1) {
            issue.setMilestone(milestones.get(0));
            return;
        }

        // Find milestones containing exact title
        milestones = model.getMilestones().stream()
            .filter(m -> m.getTitle().toLowerCase().equals(content.get().toLowerCase()))
            .collect(Collectors.toList());

        if (milestones.isEmpty()) {
            throw new QualifierApplicationException("Invalid milestone " + content.get());
        } else if (milestones.size() == 1) {
            issue.setMilestone(milestones.get(0));
            return;
        }

        throw new QualifierApplicationException(
            "Ambiguous filter: can apply any of the following milestones: " + milestones.toString());
    }

    private void applyLabel(TurboIssue issue, IModel model) throws QualifierApplicationException {
        if (!content.isPresent()) {
            throw new QualifierApplicationException("Name of label to apply required");
        }

        // Find labels containing the label name
        List<TurboLabel> labels = model.getLabels().stream()
           .filter(l -> l.getActualName().toLowerCase().contains(content.get().toLowerCase()))
            .collect(Collectors.toList());

        if (labels.isEmpty()) {
            throw new QualifierApplicationException("Invalid label " + content.get());
        } else if (labels.size() == 1) {
            issue.addLabel(labels.get(0));
            return;
        }

        // Find labels with the exact label name
        labels = model.getLabels().stream()
            .filter(l -> l.getActualName().toLowerCase().equals(content.get().toLowerCase()))
            .collect(Collectors.toList());

        if (labels.isEmpty()) {
            throw new QualifierApplicationException("Invalid label " + content.get());
        } else if (labels.size() == 1) {
            issue.addLabel(labels.get(0));
            return;
        }

        throw new QualifierApplicationException(
            "Ambiguous filter: can apply any of the following labels: " + labels.toString());
    }

    private void applyAssignee(TurboIssue issue, IModel model) throws QualifierApplicationException {
        if (!content.isPresent()) {
            throw new QualifierApplicationException("Name of assignee to apply required");
        }

        // Find assignees containing partial name
        List<TurboUser> assignees = model.getUsers().stream()
            .filter(c -> c.getLoginName().toLowerCase().contains(content.get().toLowerCase()))
            .collect(Collectors.toList());

        if (assignees.isEmpty()) {
            throw new QualifierApplicationException("Invalid user " + content.get());
        } else if (assignees.size() == 1) {
            issue.setAssignee(assignees.get(0));
            return;
        }

        // Find assignees containing partial name
        assignees = model.getUsers().stream()
            .filter(c -> c.getLoginName().toLowerCase().equals(content.get().toLowerCase()))
            .collect(Collectors.toList());

        if (assignees.isEmpty()) {
            throw new QualifierApplicationException("Invalid user " + content.get());
        } else if (assignees.size() == 1) {
            issue.setAssignee(assignees.get(0));
            return;
        }

        throw new QualifierApplicationException(
            "Ambiguous filter: can apply any of the following assignees: " + assignees.toString());
    }

    private void applyState(TurboIssue issue) throws QualifierApplicationException {
        if (!content.isPresent()) {
            throw new QualifierApplicationException("State to apply required");
        }

        if (content.get().toLowerCase().contains("open")) {
            issue.setOpen(true);
        } else if (content.get().toLowerCase().contains("closed")) {
            issue.setOpen(false);
        } else {
            throw new QualifierApplicationException("Invalid state " + content.get());
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
