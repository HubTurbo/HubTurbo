package filter.expression;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import backend.resource.*;
import filter.*;
import util.Utility;
import backend.interfaces.IModel;

public class Qualifier implements FilterExpression {

    public static final Qualifier EMPTY = new Qualifier(QualifierType.EMPTY, "");
    public static final Qualifier FALSE = new Qualifier(QualifierType.FALSE, "");
    public static final String USER_WARNING_ERROR_FORMAT = "Cannot find username containing %s in %s%n";

    private final QualifierType type;

    // Only one of these will be present at a time
    private Optional<DateRange> dateRange = Optional.empty();
    private Optional<String> content = Optional.empty();
    private Optional<LocalDate> date = Optional.empty();
    private Optional<NumberRange> numberRange = Optional.empty();
    private Optional<Integer> number = Optional.empty();
    private List<SortKey> sortKeys = new ArrayList<>();

    // Copy constructor
    public Qualifier(Qualifier other) {
        this.type = other.getType();
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

    public Qualifier(QualifierType type, String content) {
        this.type = type;
        this.content = Optional.of(content);
    }

    public Qualifier(QualifierType type, NumberRange numberRange) {
        this.type = type;
        this.numberRange = Optional.of(numberRange);
    }

    public Qualifier(QualifierType type, DateRange dateRange) {
        this.type = type;
        this.dateRange = Optional.of(dateRange);
    }

    public Qualifier(QualifierType type, LocalDate date) {
        this.type = type;
        this.date = Optional.of(date);
    }

    public Qualifier(QualifierType type, int number) {
        this.type = type;
        this.number = Optional.of(number);
    }

    public Qualifier(QualifierType type, List<SortKey> keys) {
        this.type = type;
        this.sortKeys = new ArrayList<>(keys);
    }

    public static FilterExpression replaceMilestoneAliases(IModel model, FilterExpression expr) {
        List<String> repoIds = getMetaQualifierContent(expr, QualifierType.REPO).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        if (repoIds.isEmpty()) {
            repoIds.add(model.getDefaultRepo().toLowerCase());
        }

        List<TurboMilestone> milestonesOfReposInPanel = TurboMilestone.filterMilestonesOfRepos(
                model.getMilestones(), repoIds);
        List<TurboMilestone> aliasableMilestones = getAliasableMilestones(milestonesOfReposInPanel);
        Map<Integer, TurboMilestone> milestoneAliasIndex = getMilestoneAliasIndex(aliasableMilestones);

        if (milestoneAliasIndex.isEmpty()) {
            return expr;
        }

        return expr.map(q -> {
            if (Qualifier.isMilestoneQualifier(q)) {
                return q.convertMilestoneAliasQualifier(milestoneAliasIndex);
            } else {
                return q;
            }
        });
    }

    /**
     * Expands aliases for Qualifier keyword in user input.
     * Includes aliases for Qualifier that also functions as keyword
     *
     * @param input
     * @return
     */
    public static String expandKeywordAliases(String input) {

        switch (input) {
        case "as":
            return "assignee";
        case "body":
        case "desc":
        case "de":
            return "description";
        case "d":
            return "date";
        case "l":
        case "labels":
            return "label";
        case "m":
        case "milestones":
            return "milestone";
        case "r":
            return "repo";
        case "st":
        case "status":
            return "state";
        case "t":
            return "title";
        case "u":
            return "updated";
        case "o":
            return "open";
        case "c":
            return "closed";
        case "i":
            return "issue";
        case "p":
        case "pullrequest":
            return "pr";
        case "mg":
            return "merged";
        case "um":
            return "unmerged";
        case "rd":
            return "read";
        case "ur":
            return "unread";
        case "cm":
            return "comments";
        case "ns":
            return "nonSelfUpdate";
        default:
            return input;
        }
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
                .map(Qualifier::getType)
                .collect(Collectors.toList())
                .contains(QualifierType.REPO);

        if (!containsRepoQualifier) {
            exprWithNormalQualifiers = new Conjunction(
                    new Qualifier(QualifierType.REPO, model.getDefaultRepo()),
                    exprWithNormalQualifiers);
        }

        return exprWithNormalQualifiers.isSatisfiedBy(model, issue, new MetaQualifierInfo(metaQualifiers));
    }

    /**
     * Get all milestones which milestone alias (current+-[n]) can resolve to. This will henceforth
     * be called aliasable milestones.
     * <p>
     * A milestone is aliasable if it has due date or it is the only open milestone in that repo.
     */
    private static List<TurboMilestone> getAliasableMilestones(List<TurboMilestone> milestonesOfReposInPanel) {
        List<TurboMilestone> milestones = new ArrayList<>();
        // add milestones with due date first
        milestones.addAll(milestonesOfReposInPanel.stream()
                .filter(ms -> ms.getDueDate().isPresent())
                .collect(Collectors.toList()));

        // Special case: if there is only one open milestone, it should be included regardless of whether
        // it has due date or not.
        // In this case, if it is not included already (the open milestone does not
        // have due date), add it to the milestone list.
        List<TurboMilestone> openMilestones = TurboMilestone.filterOpenMilestones(milestonesOfReposInPanel);
        if (openMilestones.size() == 1) {
            TurboMilestone openMilestone = openMilestones.get(0);
            boolean hasDueDate = openMilestone.getDueDate().isPresent();
            if (!hasDueDate) {
                milestones.add(openMilestone);
            }
        }

        return milestones;
    }

    /**
     * Returns a map where the key denotes offset from "current" milestone.
     * i.e. 0 for "current" milestone, -1 for "current-1" milestone, etc.
     */
    private static Map<Integer, TurboMilestone> getMilestoneAliasIndex(List<TurboMilestone> milestones) {
        List<TurboMilestone> sortedMilestones = TurboMilestone.sortByDueDate(milestones);
        Optional<Integer> currentMilestoneIndex = getCurrentMilestoneIndex(sortedMilestones);

        assert currentMilestoneIndex.isPresent() || !currentMilestoneIndex.isPresent() && sortedMilestones.isEmpty();

        return IntStream
                .range(0, sortedMilestones.size())
                .boxed()
                .collect(Collectors.toMap(i -> i - currentMilestoneIndex.get(), i -> sortedMilestones.get(i)));
    }

    /**
     * "Current" milestone is an ongoing milestone with the earliest due date. However, if there
     * is only one open milestone, it will be considered as the "current" milestone, even if it
     * does not have a due date.
     * <p>
     * If there is no ongoing or open milestone, sets "current" as one after last milestone -
     * this means that there is no "current" milestone, but it is possible that there is a
     * milestone before the "current" one (i.e. current-[n] where n >= 1)
     * <p>
     * This method expects the milestone list to be sorted (by due date)
     */
    private static Optional<Integer> getCurrentMilestoneIndex(List<TurboMilestone> sortedMilestones) {
        if (sortedMilestones.isEmpty()) {
            return Optional.empty();
        }

        int openMilestonesCount = TurboMilestone.filterOpenMilestones(sortedMilestones).size();
        if (openMilestonesCount == 1) {
            return getFirstOpenMilestonePosition(sortedMilestones);
        }

        // Look for the first milestone in the (sorted) list that is ongoing.
        Optional<Integer> firstOngoingMilestonePosition = getFirstOngoingMilestonePosition(sortedMilestones);

        // if no ongoing milestone, set current as one after last milestone
        return firstOngoingMilestonePosition.isPresent()
                ? firstOngoingMilestonePosition
                : Optional.of(sortedMilestones.size());
    }

    /**
     * Expects milestone to be sorted (by due date).
     */
    private static Optional<Integer> getFirstOpenMilestonePosition(List<TurboMilestone> milestones) {
        return IntStream
                .range(0, milestones.size())
                .boxed()
                .filter(i -> milestones.get(i).isOpen())
                .findFirst();
    }

    /**
     * Expects milestone to be sorted (by due date).
     */
    private static Optional<Integer> getFirstOngoingMilestonePosition(List<TurboMilestone> milestones) {
        return IntStream
                .range(0, milestones.size())
                .boxed()
                .filter(i -> milestones.get(i).isOngoing())
                .findFirst();
    }

    public static HashSet<String> getMetaQualifierContent(FilterExpression expr, QualifierType qualifierType) {
        HashSet<String> contents = new HashSet<>();
        List<Qualifier> panelMetaQualifiers = expr.find(Qualifier::isMetaQualifier);

        panelMetaQualifiers.forEach(metaQualifier -> {
            if (metaQualifier.getType().equals(qualifierType) && metaQualifier.getContent().isPresent()) {
                contents.add(metaQualifier.getContent().get());
            }
        });

        return contents;
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

    @Override
    public boolean isEmpty() {
        return type == QualifierType.EMPTY;
    }

    public boolean isFalse() {
        return type == QualifierType.FALSE;
    }

    @Override
    public boolean isSatisfiedBy(IModel model, TurboIssue issue, MetaQualifierInfo info) {
        assert type != null;

        // The empty qualifier is satisfied by anything
        if (isEmpty()) return true;

        // The false qualifier is satisfied by nothing
        if (isFalse()) return false;

        switch (type) {
        case ID:
            return idSatisfies(issue);
        case KEYWORD:
            return keywordSatisfies(issue, info);
        case TITLE:
            return titleSatisfies(issue);
        case DESCRIPTION:
            return bodySatisfies(issue);
        case MILESTONE:
            return milestoneSatisfies(model, issue);
        case LABEL:
            return labelsSatisfy(model, issue);
        case AUTHOR:
            return authorSatisfies(issue);
        case ASSIGNEE:
            return assigneeSatisfies(model, issue);
        case INVOLVES:
            return involvesSatisfies(model, issue);
        case TYPE:
            return typeSatisfies(issue);
        case STATE:
            return stateSatisfies(issue);
        case HAS:
            return satisfiesHasConditions(issue);
        case NO:
            return satisfiesNoConditions(issue);
        case IS:
            return satisfiesIsConditions(issue);
        case CREATED:
            return satisfiesCreationDate(issue);
        case UPDATED:
            return satisfiesUpdatedHours(issue);
        case REPO:
            return satisfiesRepo(issue);
        default:
            assert false : "Missing case for " + type;
            return false;
        }
    }

    @Override
    public void applyTo(TurboIssue issue, IModel model) throws QualifierApplicationException {
        assert type != null && content != null;

        // The empty qualifier should not be applied to anything
        assert !isEmpty();

        // The false qualifier should not be applied to anything
        assert !isFalse();

        switch (type) {
        case TITLE:
        case DESCRIPTION:
        case KEYWORD:
            throw new QualifierApplicationException(
                    "Unnecessary filter: issue text cannot be changed by dragging");
        case ID:
            throw new QualifierApplicationException("Unnecessary filter: id is immutable");
        case CREATED:
            throw new QualifierApplicationException(
                    "Unnecessary filter: cannot change issue creation date");
        case HAS:
        case NO:
        case IS:
            throw new QualifierApplicationException("Ambiguous filter: " + type);
        case MILESTONE:
            applyMilestone(issue, model);
            break;
        case LABEL:
            applyLabel(issue, model);
            break;
        case ASSIGNEE:
            applyAssignee(issue, model);
            break;
        case AUTHOR:
            throw new QualifierApplicationException(
                    "Unnecessary filter: cannot change author of issue");
        case INVOLVES:
            throw new QualifierApplicationException(
                    "Ambiguous filter: cannot change users involved with issue");
        case STATE:
            applyState(issue);
            break;
        default:
            assert false : "Missing case for " + type;
            break;
        }
    }

    @Override
    public List<String> getWarnings(IModel model, TurboIssue issue) {
        switch (type) {
        case AUTHOR:
        case ASSIGNEE:
        case INVOLVES:
            return getWarningsForTypeAuthorOrAssignee(model, issue);
        default:
            return new ArrayList<>();
        }
    }

    /**
     * If the user referred in the qualifier is not a contributor of the repository of the TurboIssue,
     * it returns a List<String> containing a warning that explains so, or an empty List<String> otherwise.
     */
    private List<String> getWarningsForTypeAuthorOrAssignee(IModel model, TurboIssue issue) {
        List<String> result = new ArrayList<>();
        if (content.isPresent() && !model.isUserInRepo(issue.getRepoId(), content.get())) {
            result.add(String.format(USER_WARNING_ERROR_FORMAT, content.get(), issue.getRepoId()));
        }
        return result;
    }

    @Override
    public boolean canBeAppliedToIssue() {
        return true;
    }

    @Override
    public List<QualifierType> getQualifierTypes() {
        return new ArrayList<>(Arrays.asList(type));
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
        if (this.isEmpty()) {
            return "";
        } else if (content.isPresent()) {
            String quotedContent = content.get();
            if (quotedContent.contains(" ")) {
                quotedContent = "\"" + quotedContent + "\"";
            }
            if (type == QualifierType.KEYWORD) {
                return quotedContent;
            } else {
                return type + ":" + quotedContent;
            }
        } else if (date.isPresent()) {
            return type + ":" + date.get().toString();
        } else if (dateRange.isPresent()) {
            return type + ":" + dateRange.get().toString();
        } else if (!sortKeys.isEmpty()) {
            return type + ":" + sortKeys.stream().map(SortKey::toString).collect(Collectors.joining(","));
        } else if (numberRange.isPresent()) {
            return type + ":" + numberRange.get().toString();
        } else if (number.isPresent()) {
            return type + ":" + number.get().toString();
        } else {
            assert false : "Should not happen";
            return "";
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
                type.equals(other.type);
    }

    private static boolean shouldNotBeStripped(Qualifier q) {
        return !shouldBeStripped(q);
    }

    private static boolean shouldBeStripped(Qualifier q) {
        switch (q.getType()) {
        case IN:
        case SORT:
        case COUNT:
            return true;
        default:
            return false;
        }
    }

    public static boolean isMetaQualifier(Qualifier q) {
        switch (q.getType()) {
        case SORT:
        case IN:
        case REPO:
        case COUNT:
            return true;
        default:
            return isUpdatedQualifier(q);
        }
    }

    public static boolean isMilestoneQualifier(Qualifier q) {
        switch (q.getType()) {
        case MILESTONE:
            return true;
        default:
            return false;
        }
    }

    public static boolean isUpdatedQualifier(Qualifier q) {
        switch (q.getType()) {
        case UPDATED:
            return true;
        default:
            return false;
        }
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

        switch (expandKeywordAliases(key)) {
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
                    return -(TurboMilestone.getDueDateComparator().compare(aMilestone.get(), bMilestone.get()));
                }
            };
            break;
        case "id":
            comparator = (a, b) -> a.getId() - b.getId();
            break;
        case "state":
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
                    l.isInGroup() && l.getGroupName().equals(group);

            Comparator<TurboLabel> labelComparator = (x, y) -> x.compareTo(y);

            List<TurboLabel> aLabels = model.getLabelsOfIssue(a, sameGroup);
            List<TurboLabel> bLabels = model.getLabelsOfIssue(b, sameGroup);
            Collections.sort(aLabels, labelComparator);
            Collections.sort(bLabels, labelComparator);

            // Put empty lists at the back
            if (aLabels.isEmpty() && bLabels.isEmpty()) {
                return 0;
            } else if (aLabels.isEmpty()) {
                // a is larger
                return 1;
            } else if (bLabels.isEmpty()) {
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
        throw new SemanticException(type);
    }

    private boolean satisfiesUpdatedHours(TurboIssue issue) {
        NumberRange updatedRange;

        if (numberRange.isPresent()) {
            updatedRange = numberRange.get();
        } else if (number.isPresent()) {
            updatedRange = new NumberRange(null, number.get(), true);
        } else {
            throw new SemanticException(type);
        }

        LocalDateTime dateOfUpdate = issue.getUpdatedAt();
        int hoursSinceUpdate = Utility.safeLongToInt(dateOfUpdate.until(getCurrentTime(), ChronoUnit.HOURS));
        return updatedRange.encloses(hoursSinceUpdate);
    }

    private boolean satisfiesRepo(TurboIssue issue) {
        if (!content.isPresent()) throw new SemanticException(type);

        return issue.getRepoId().equalsIgnoreCase(content.get());
    }

    private boolean satisfiesCreationDate(TurboIssue issue) {
        LocalDate creationDate = issue.getCreatedAt().toLocalDate();
        if (date.isPresent()) {
            return creationDate.isEqual(date.get());
        } else if (dateRange.isPresent()) {
            return dateRange.get().encloses(creationDate);
        } else {
            throw new SemanticException(type);
        }
    }

    private boolean satisfiesHasConditions(TurboIssue issue) {
        if (!content.isPresent()) throw new SemanticException(type);

        switch (expandKeywordAliases(content.get())) {
        case "label":
            return issue.getLabels().size() > 0;
        case "milestone":
            assert issue.getMilestone() != null;
            return issue.getMilestone().isPresent();
        case "assignee":
            assert issue.getAssignee() != null;
            return issue.getAssignee().isPresent();
        default:
            throw new SemanticException(type);
        }
    }

    private boolean satisfiesNoConditions(TurboIssue issue) {
        return content.isPresent() && !satisfiesHasConditions(issue);
    }

    private boolean satisfiesIsConditions(TurboIssue issue) {
        if (!content.isPresent()) throw new SemanticException(type);

        switch (expandKeywordAliases(content.get())) {
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
            throw new SemanticException(type);
        }
    }

    private boolean stateSatisfies(TurboIssue issue) {
        if (!content.isPresent()) throw new SemanticException(type);

        String content = expandKeywordAliases(this.content.get().toLowerCase());
        if (content.contains("open")) {
            return issue.isOpen();
        } else if (content.contains("closed")) {
            return !issue.isOpen();
        } else {
            throw new SemanticException(type);
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
        if (inputLabel.isInGroup()) {
            group = inputLabel.getGroupName();
        }
        String labelName = inputLabel.getShortName();

        if (candidateLabel.isInGroup()) {
            if (labelName.isEmpty()) {
                // Check the group
                if (candidateLabel.getGroupName().contains(group)) {
                    return true;
                }
            } else {
                if (candidateLabel.getGroupName().contains(group)
                        && candidateLabel.getShortName().contains(labelName)) {
                    return true;
                }
            }
        } else {
            // Check only the label name
            if (group.isEmpty() && !labelName.isEmpty() && candidateLabel.getShortName().contains(labelName)) {
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
            if (labelMatches(content.get(), label.getFullName())) {
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
            switch (expandKeywordAliases(info.getIn().get())) {
            case "title":
                return titleSatisfies(issue);
            case "description":
                return bodySatisfies(issue);
            default:
                throw new SemanticException(QualifierType.IN);
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
        if (!content.isPresent()) throw new SemanticException(type);
        String content = this.content.get().toLowerCase();
        switch (expandKeywordAliases(content)) {
        case "issue":
            return !issue.isPullRequest();
        case "pr":
            return issue.isPullRequest();
        default:
            throw new SemanticException(type);
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
                .filter(l -> l.getFullName().toLowerCase().contains(content.get().toLowerCase()))
                .collect(Collectors.toList());

        if (labels.isEmpty()) {
            throw new QualifierApplicationException("Invalid label " + content.get());
        } else if (labels.size() == 1) {
            issue.addLabel(labels.get(0));
            return;
        }

        // Find labels with the exact label name
        labels = model.getLabels().stream()
                .filter(l -> l.getFullName().toLowerCase().equals(content.get().toLowerCase()))
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

    public QualifierType getType() {
        return type;
    }

    private Qualifier convertMilestoneAliasQualifier(Map<Integer, TurboMilestone> milestoneAliasIndex) {
        if (!content.isPresent()) {
            return Qualifier.EMPTY;
        }

        String contents = content.get();
        String alias = contents;

        Pattern aliasPattern = Pattern.compile("(curr(?:ent)?)(?:(\\+|-)(\\d+))?$");
        Matcher aliasMatcher = aliasPattern.matcher(alias);

        if (!aliasMatcher.find()) {
            return this;
        }

        int offset = 0;
        if (aliasMatcher.group(2) != null && aliasMatcher.group(3) != null) {
            if (aliasMatcher.group(2).equals("+")) {
                offset = Integer.parseInt(aliasMatcher.group(3));
            } else {
                offset = -Integer.parseInt(aliasMatcher.group(3));
            }
        }

        // if the offset is not within alias range, no issue will be shown
        if (!milestoneAliasIndex.containsKey(offset)) {
            return Qualifier.FALSE;
        }

        contents = milestoneAliasIndex.get(offset).getTitle().toLowerCase();

        return new Qualifier(type, contents);
    }

    /**
     * Determines the count value to be taken from the count qualifier. Throw a ParseException if count
     * qualifier is not valid.
     *
     * @param issueList  Issue list obtained after filtering and sorting.
     * @param filterExpr The filter expression of the particular panel.
     * @return The valid count value in the qualifier or the issueList.size() by default
     */
    public static int determineCount(List<TurboIssue> issueList, FilterExpression filterExpr) {
        List<Qualifier> countQualifiers = filterExpr.find(Qualifier::isMetaQualifier).stream()
                .filter(q -> q.getType() == QualifierType.COUNT)
                .collect(Collectors.toList());
        if (countQualifiers.isEmpty()) {
            return issueList.size();
        } else if (countQualifiers.size() > 1) {
            throw new ParseException("More than one count qualifier");
        } else if (!countQualifiers.get(0).getNumber().isPresent()) {
            throw new ParseException("Count qualifier should be a number greater than or equal to 0");
        } else {
            return countQualifiers.get(0).getNumber().get();
        }
    }
}
