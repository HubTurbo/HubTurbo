package filter.expression;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import backend.resource.*;
import util.Utility;
import backend.interfaces.IModel;
import filter.MetaQualifierInfo;
import filter.QualifierApplicationException;


public class Qualifier implements FilterExpression {

    public static final String REPO = "repo";
    public static final String SORT = "sort";
    private static final String UPDATED = "updated";

    public static final Qualifier EMPTY = new Qualifier("", "");
    public static final Qualifier FALSE = new Qualifier("false", "");

    public static final List<String> KEYWORDS = Collections.unmodifiableList(Arrays.asList(
        "assignee", "author", "body", "closed", "comments", "created", "creator",
        "date", "desc", "description", "false", "has", "id", "in", "involves",
        "is", "issue", "keyword", "label", "labels", "merged", "milestone", "milestones",
        "no", "nonSelfUpdate", "open", "pr", "pullrequest", "read", REPO, SORT, "state", "status",
        "title", "type", "unmerged", "unread", UPDATED, "user"
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

    public static FilterExpression replaceMilestoneAliases(IModel model, FilterExpression expr) {
        List<String> repoIds = getContentOfMetaQualifier(expr, REPO).stream()
                .map(repoId -> repoId.toLowerCase())
                .collect(Collectors.toList());

        if (repoIds.isEmpty()) {
            repoIds.add(model.getDefaultRepo().toLowerCase());
        }

        List<TurboMilestone> allMilestones = model.getMilestones().stream()
                .filter(ms -> ms.getDueDate().isPresent())
                .filter(ms -> repoIds.contains(ms.getRepoId().toLowerCase()))
                .sorted((a, b) -> getMilestoneDueDateComparator().compare(a, b))
                .collect(Collectors.toList());

        Optional<Integer> currentMilestoneIndex = getCurrentMilestoneIndex(allMilestones);

        if (!currentMilestoneIndex.isPresent()) {
            return expr;
        }

        expr = expr.map(q -> {
            if (Qualifier.isMilestoneQualifier(q)) {
                return q.convertMilestoneAliasQualifier(allMilestones, currentMilestoneIndex.get());
            } else {
                return q;
            }
        });

        return expr;
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

    private static Optional<Integer> getCurrentMilestoneIndex(List<TurboMilestone> allMilestones) {
        if (allMilestones.isEmpty()) {
            return Optional.empty();
        }

        int currentIndex = 0;

        for (TurboMilestone checker : allMilestones) {
            boolean overdue = checker.getDueDate().isPresent() &&
                    checker.getDueDate().get().isBefore(LocalDate.now());
            boolean relevant = !(overdue && (checker.getOpenIssues() == 0));

            if (checker.isOpen() && relevant) {
                return Optional.of(currentIndex);
            }

            currentIndex++;
        }

        // if no open milestone, set current as one after last milestone
        // - this means that no such milestone, which will return no issue
        return Optional.of(allMilestones.size());
    }

    public static void processMetaQualifierEffects(FilterExpression expr,
                                                   BiConsumer<Qualifier, MetaQualifierInfo> callback) {

        List<Qualifier> qualifiers = expr.find(Qualifier::isMetaQualifier);
        MetaQualifierInfo info = new MetaQualifierInfo(qualifiers);
        qualifiers.forEach(q -> callback.accept(q, info));
    }

    public static HashSet<String> getContentOfMetaQualifier(FilterExpression expr, String qualifierName) {
        HashSet<String> contentsOfMetaQualifier = new HashSet<>();
        List<Qualifier> panelMetaQualifiers = expr.find(Qualifier::isMetaQualifier);

        panelMetaQualifiers.forEach(metaQualifier -> {
            if (metaQualifier.getName().equals(qualifierName) && metaQualifier.getContent().isPresent()) {
                contentsOfMetaQualifier.add(metaQualifier.getContent().get());
            }
        });

        return contentsOfMetaQualifier;
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

    public boolean isFalseQualifier() {
        return name.equalsIgnoreCase("false") && content.isPresent() && content.get().isEmpty();
    }

    @Override
    public boolean isSatisfiedBy(IModel model, TurboIssue issue, MetaQualifierInfo info) {
        assert name != null;

        // The empty qualifier is satisfied by anything
        if (isEmptyQualifier()) return true;

        // The false qualifier is satisfied by nothing
        if (isFalseQualifier()) return false;

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
        case "m":
            return milestoneSatisfies(model, issue);
        case "label":
            return labelsSatisfy(model, issue);
        case "author":
        case "creator":
        case "au":
            return authorSatisfies(issue);
        case "assignee":
        case "as":
            return assigneeSatisfies(model, issue);
        case "involves":
        case "user":
            return involvesSatisfies(model, issue);
        case "type":
            return typeSatisfies(issue);
        case "state":
        case "status":
        case "s":
            return stateSatisfies(issue);
        case "has":
            return satisfiesHasConditions(issue);
        case "no":
            return satisfiesNoConditions(issue);
        case "is":
            return satisfiesIsConditions(issue);
        case "created":
            return satisfiesCreationDate(issue);
        case UPDATED:
            return satisfiesUpdatedHours(issue);
        case REPO:
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

        // The false qualifier should not be applied to anything
        assert !isFalseQualifier();

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
        case "m":
            applyMilestone(issue, model);
            break;
        case "label":
            applyLabel(issue, model);
            break;
        case "assignee":
        case "as":
            applyAssignee(issue, model);
            break;
        case "author":
        case "creator":
        case "au":
            throw new QualifierApplicationException("Unnecessary filter: cannot change author of issue");
        case "involves":
        case "user":
            throw new QualifierApplicationException("Ambiguous filter: cannot change users involved with issue");
        case "state":
        case "status":
        case "s":
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

    @Override
    public FilterExpression map(Function<Qualifier, Qualifier> func) {
        return func.apply(this);
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
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
            case SORT:
                return true;
            default:
                return false;
        }
    }

    public static boolean isMetaQualifier(Qualifier q) {
        switch (q.getName()) {
        case SORT:
        case "in":
        case REPO:
            return true;
        default:
            return isUpdatedQualifier(q);
        }
    }

    public static boolean isMilestoneQualifier(Qualifier q) {
        switch (q.getName()) {
            case "milestone":
            case "m":
                return true;
            default:
                return false;
        }
    }

    public static boolean isUpdatedQualifier(Qualifier q) {
        switch (q.getName()) {
            case UPDATED:
                return true;
            default:
                return false;
        }
    }

    public static boolean hasUpdatedQualifier(List<Qualifier> metaQualifiers) {
        return metaQualifiers.stream()
            .filter(Qualifier::isUpdatedQualifier)
            .findAny().isPresent();
    }

    public static boolean hasUpdatedQualifier(FilterExpression expr) {
        return !expr.find(Qualifier::isUpdatedQualifier).isEmpty();
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
            case UPDATED:
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
            case "assignee":
            case "as":
                comparator = (a, b) -> {
                    Optional<String> aAssignee = a.getAssignee();
                    Optional<String> bAssignee = b.getAssignee();

                    if (!aAssignee.isPresent() && !bAssignee.isPresent()) {
                        return 0;
                    } else if (!aAssignee.isPresent()) {
                        return 1;
                    } else if (!bAssignee.isPresent()) {
                        return -1;
                    } else {
                        return aAssignee.get().compareTo(bAssignee.get());
                    }
                };
                break;
            case "milestone":
            case "m":
                comparator = (a, b) -> {
                    Optional<TurboMilestone> aMilestone = model.getMilestoneOfIssue(a);
                    Optional<TurboMilestone> bMilestone = model.getMilestoneOfIssue(b);

                    if (!aMilestone.isPresent() && !bMilestone.isPresent()) {
                        return 0;
                    } else if (!aMilestone.isPresent()) {
                        return 1;
                    } else if (!bMilestone.isPresent()) {
                        return -1;
                    } else {
                        Optional<LocalDate> aDueDate = aMilestone.get().getDueDate();
                        Optional<LocalDate> bDueDate = bMilestone.get().getDueDate();

                        if (!aDueDate.isPresent() && !bDueDate.isPresent()) {
                            return 0;
                        } else if (!aDueDate.isPresent()) {
                            return 1;
                        } else if (!bDueDate.isPresent()) {
                            return -1;
                        } else {
                            return -(getMilestoneDueDateComparator()
                                    .compare(aMilestone.get(), bMilestone.get()));
                        }
                    }
                };
                break;
            case "id":
                comparator = (a, b) -> a.getId() - b.getId();
                break;
            case "status":
            case "s":
                comparator = (a, b) -> Boolean.compare(b.isOpen(), a.isOpen());
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

        LocalDateTime dateOfUpdate = issue.getUpdatedAt();
        int hoursSinceUpdate = Utility.safeLongToInt(dateOfUpdate.until(getCurrentTime(), ChronoUnit.HOURS));
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
        case "m":
            assert issue.getMilestone() != null;
            return issue.getMilestone().isPresent();
        case "assignee":
        case "assignees":
        case "as":
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

    public static boolean labelMatches(String input, String candidate) {

        // Make use of TurboLabel constructor to parse the input, avoiding duplication
        TurboLabel inputLabel = new TurboLabel("", input.toLowerCase());
        TurboLabel candidateLabel = new TurboLabel("", candidate.toLowerCase());

        String group = "";
        if (inputLabel.hasGroup()) {
            group = inputLabel.getGroup().get();
        }
        String labelName = inputLabel.getName();

        if (candidateLabel.hasGroup()) {
            if (labelName.isEmpty()) {
                // Check the group
                if (candidateLabel.getGroup().get().contains(group)) {
                    return true;
                }
            } else {
                if (candidateLabel.getGroup().get().contains(group)
                    && candidateLabel.getName().contains(labelName)) {
                    return true;
                }
            }
        } else {
            // Check only the label name
            if (group.isEmpty() && !labelName.isEmpty() && candidateLabel.getName().contains(labelName)) {
                return true;
            }
        }
        return false;
    }

    private boolean labelsSatisfy(IModel model, TurboIssue issue) {
        if (!content.isPresent()) return false;

        // A qualifier matches an issue if the issue is associated with some subset of the
        // labels that the qualifier expresses. It should only reject an issue if the issue
        // does not contain any labels it expresses, and not if the issue contains some label
        // it does not express.

        for (TurboLabel label : model.getLabelsOfIssue(issue)) {
            if (labelMatches(content.get(), label.getActualName())) {
                return true;
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

    private Qualifier convertMilestoneAliasQualifier(List<TurboMilestone> allMilestones, int currentIndex) {
        if (!content.isPresent()) {
            return Qualifier.EMPTY;
        }

        String contents = content.get();
        String alias = contents;

        Pattern aliasPattern = Pattern.compile("(curr(?:ent)?)(?:(\\+|-)(\\d+))?$");
        Matcher aliasMatcher = aliasPattern.matcher(alias);
        int offset = 0;

        if (!aliasMatcher.find()) {
            return this;
        }

        if (aliasMatcher.group(2) != null && aliasMatcher.group(3) != null) {
            offset = Integer.parseInt(aliasMatcher.group(3));
            if (aliasMatcher.group(2).equals("+")) {
                currentIndex += offset;
            } else {
                currentIndex -= offset;
            }
        }

        // if out of milestone range, don't convert alias
        if (currentIndex >= allMilestones.size() || currentIndex < 0) {
            return Qualifier.FALSE;
        }

        contents = allMilestones.get(currentIndex).getTitle().toLowerCase();

        return new Qualifier(name, contents);
    }

    /**
     * Condition: milestone must have due dates
     */
    public static Comparator<TurboMilestone> getMilestoneDueDateComparator() {
        return (a, b) -> {
            assert(a.getDueDate().isPresent());
            assert(b.getDueDate().isPresent());
            LocalDate aDueDate = a.getDueDate().get();
            LocalDate bDueDate = b.getDueDate().get();

            return (aDueDate.compareTo(bDueDate));
        };
    }
}
